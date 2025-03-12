package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.image.service.ImageService;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.util.ExcelMaker;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.repository.TaxInvoiceFileRepository;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.dto.BoundingPoly;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.dto.TemplateOcrField;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.infrastructure.ClovaOcrApi;
import Seoul_Milk.sm_server.global.infrastructure.clovaOcr.service.OcrDataExtractor;
import Seoul_Milk.sm_server.global.common.exception.CustomException;
import Seoul_Milk.sm_server.global.common.exception.ErrorCode;
import Seoul_Milk.sm_server.global.infrastructure.upload.service.AwsS3Service;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static Seoul_Milk.sm_server.global.common.exception.ErrorCode.MAKE_EXCEL_FILE_ERROR;

@Service
@RequiredArgsConstructor
public class TaxInvoiceServiceImpl implements TaxInvoiceService {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final ClovaOcrApi clovaOcrApi;
    private final OcrDataExtractor ocrDataExtractor;
    private final TaxInvoiceRepository taxInvoiceRepository;
    private final TaxInvoiceFileRepository taxInvoiceFileRepository;
    private final ImageService imageService;
    private final AwsS3Service awsS3Service;

    private static final int MAX_REQUESTS_PER_SECOND = 5;  // 초당 최대 5개 요청
    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND, true);

    //excelMaker 주입
    private final ExcelMaker excelMaker;


    /**
     * CLOCA OCR TEMPLATE 유형으로 보내는 요청에 맞게 처리
     */
    @Async("ocrTaskExecutor")
    @Override
    public CompletableFuture<TaxInvoiceResponseDTO.Create> processTemplateOcrAsync(MultipartFile image, MemberEntity member) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                semaphore.acquire(); // 요청 개수가 5개를 초과하면 자동 대기

                long startTime = System.nanoTime();
                TaxInvoiceResponseDTO.Create response = processTemplateOcrSync(image, member);
                long endTime = System.nanoTime();

                long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                System.out.println("OCR 요청 처리 시간: " + elapsedTimeMillis + " ms");

                return response;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return TaxInvoiceResponseDTO.Create.error(image.getOriginalFilename(), "OCR 요청 대기 중 인터럽트 발생");
            } finally {
                semaphore.release(); // 실행이 끝나면 세마포어 해제
            }
        });
    }

    @Transactional
    public TaxInvoiceResponseDTO.Create processTemplateOcrSync(MultipartFile image, MemberEntity member) {
        long startTime = System.nanoTime();
        List<String> errorDetails = new ArrayList<>();
        List<String> requiredFieldErrors = new ArrayList<>();
        Map<String, Object> extractedData;

        try {
            // CLOVA OCR API 호출
            String jsonResponse = clovaOcrApi.callApi("POST", image, clovaSecretKey, image.getContentType());
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new CustomException(ErrorCode.OCR_NO_RESULT);
            }

            // OCR 데이터 변환
            List<TemplateOcrField> ocrFields = convertToTemplateOcrFields(jsonResponse);
            extractedData = ocrDataExtractor.extractDataFromTemplateOcrFields(ocrFields);
            if (extractedData == null || extractedData.isEmpty()) {
                throw new CustomException(ErrorCode.OCR_EMPTY_JSON);
            }

            // OCR 데이터 검증 및 기본값 설정
            String issueId = getOrDefault(extractedData, "approval_number", "UNKNOWN");
            String ipId = getOrDefault(extractedData, "supplier_registration_number", "UNKNOWN");
            String suId = getOrDefault(extractedData, "recipient_registration_number", "UNKNOWN");
            String erDat = getOrDefault(extractedData, "issue_date", "UNKNOWN");
            String ipBusinessName = getOrDefault(extractedData, "supplier_business_name", "UNKNOWN");
            String suBusinessName = getOrDefault(extractedData, "recipient_business_name", "UNKNOWN");
            String ipName = getOrDefault(extractedData, "supplier_name", "UNKNOWN");
            String suName = getOrDefault(extractedData, "recipient_name", "UNKNOWN");
            String ipAddress = getOrDefault(extractedData, "supplier_address", "UNKNOWN");
            String suAddress = getOrDefault(extractedData, "recipient_address", "UNKNOWN");
            String ipEmail = getOrDefault(extractedData, "supplier_email", "UNKNOWN");
            String suEmail = getOrDefault(extractedData, "recipient_email", "UNKNOWN");

            ProcessStatus status = ProcessStatus.PENDING;

            // 필수값 검증 및 오류 메시지 추가
            validateRequiredField("승인번호", issueId, requiredFieldErrors);
            validateRequiredField("공급자 등록번호", ipId, requiredFieldErrors);
            validateRequiredField("공급받는자 등록번호", suId, requiredFieldErrors);
            validateRequiredField("발행일", erDat, requiredFieldErrors);

            // 가격 변환
            int chargeTotal = parseAmount(extractedData, "chargeTotal", requiredFieldErrors);
            int taxTotal = parseAmount(extractedData, "total_amount", errorDetails);
            int grandTotal = parseAmount(extractedData, "grandTotal", errorDetails);

            // 이메일 검증
            validateEmail("공급자 이메일", ipEmail, errorDetails);
            validateEmail("공급받는자 이메일", suEmail, errorDetails);

            if (!requiredFieldErrors.isEmpty()) {
                status = ProcessStatus.UNAPPROVED;
                errorDetails.addAll(requiredFieldErrors);
            }

            // OCR 성공 후 S3 파일 저장
            String fileUrl = awsS3Service.uploadFile("tax_invoices", image, true);

            TaxInvoice taxInvoice;
            TaxInvoiceFile taxFile;

            // TaxInvoice 생성 및 저장
            if (taxInvoiceRepository.findByIssueId(issueId).isEmpty()) {
                taxInvoice = TaxInvoice.create(
                        issueId, ipId, suId, chargeTotal, taxTotal, grandTotal,
                        erDat, ipBusinessName, suBusinessName, ipName, suName, ipAddress, suAddress,
                        ipEmail, suEmail, member, errorDetails, status
                );
                TaxInvoice savedTaxInvoice = taxInvoiceRepository.save(taxInvoice);
                // TaxInvoiceFile 생성 및 저장
                taxFile = TaxInvoiceFile.create(savedTaxInvoice, fileUrl, image.getContentType(),
                        image.getOriginalFilename(), image.getSize(), LocalDateTime.now());
                taxInvoiceFileRepository.save(taxFile);
                savedTaxInvoice.attachFile(taxFile);
                taxInvoiceRepository.save(savedTaxInvoice);
            }
            else{
                taxInvoice = taxInvoiceRepository.findByIssueId(issueId).get();
                taxInvoice.update(
                        issueId, ipId, suId, chargeTotal, taxTotal, grandTotal,
                        erDat, ipBusinessName, suBusinessName, ipName, suName, ipAddress, suAddress,
                        ipEmail, suEmail, member, errorDetails, status
                );
                taxFile = taxInvoice.getFile();
                taxFile.update(taxInvoice, fileUrl, image.getContentType(), image.getOriginalFilename(), image.getSize(), LocalDateTime.now());
                taxInvoice.attachFile(taxFile);
                taxInvoice.attachMember(member);
            }

            long endTime = System.nanoTime();
            long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            return TaxInvoiceResponseDTO.Create.from(taxInvoice, image.getOriginalFilename(), extractedData, errorDetails, elapsedTimeMillis);

        } catch (Exception e) {
            System.out.println("[ERROR] OCR 처리 중 예외 발생: " + e.getMessage());
            return TaxInvoiceResponseDTO.Create.error(image.getOriginalFilename(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public CompletableFuture<TaxInvoiceResponseDTO.Create> processTemplateOcrSync(String imageUrl, MemberEntity member, Long imageId) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.nanoTime();
            List<String> errorDetails = new ArrayList<>();
            List<String> requiredFieldErrors = new ArrayList<>();
            Map<String, Object> extractedData;

            try {
                // S3에서 파일 다운로드하여 MultipartFile 변환
                MultipartFile file = awsS3Service.downloadFileFromS3(imageUrl);

                // CLOVA OCR API 요청
                String jsonResponse = clovaOcrApi.callApi("POST", file, clovaSecretKey, file.getContentType());
                if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                    throw new CustomException(ErrorCode.OCR_NO_RESULT);
                }

                // OCR 데이터 변환
                List<TemplateOcrField> ocrFields = convertToTemplateOcrFields(jsonResponse);
                extractedData = ocrDataExtractor.extractDataFromTemplateOcrFields(ocrFields);
                if (extractedData == null || extractedData.isEmpty()) {
                    throw new CustomException(ErrorCode.OCR_EMPTY_JSON);
                }

                // OCR 데이터 검증 및 기본값 설정
                String issueId = getOrDefault(extractedData, "approval_number", "UNKNOWN");
                String ipId = getOrDefault(extractedData, "supplier_registration_number", "UNKNOWN");
                String suId = getOrDefault(extractedData, "recipient_registration_number", "UNKNOWN");
                String erDat = getOrDefault(extractedData, "issue_date", "UNKNOWN");
                String ipBusinessName = getOrDefault(extractedData, "supplier_business_name", "UNKNOWN");
                String suBusinessName = getOrDefault(extractedData, "recipient_business_name", "UNKNOWN");
                String ipName = getOrDefault(extractedData, "supplier_name", "UNKNOWN");
                String suName = getOrDefault(extractedData, "recipient_name", "UNKNOWN");
                String ipAddress = getOrDefault(extractedData, "supplier_address", "UNKNOWN");
                String suAddress = getOrDefault(extractedData, "recipient_address", "UNKNOWN");
                String ipEmail = getOrDefault(extractedData, "supplier_email", "UNKNOWN");
                String suEmail = getOrDefault(extractedData, "recipient_email", "UNKNOWN");

                ProcessStatus status = ProcessStatus.PENDING;

                // 필수값 검증 및 오류 메시지 추가
                validateRequiredField("승인번호", issueId, requiredFieldErrors);
                validateRequiredField("공급자 등록번호", ipId, requiredFieldErrors);
                validateRequiredField("공급받는자 등록번호", suId, requiredFieldErrors);
                validateRequiredField("발행일", erDat, requiredFieldErrors);

                // 가격 변환
                int chargeTotal = parseAmount(extractedData, "chargeTotal", requiredFieldErrors);
                int taxTotal = parseAmount(extractedData, "total_amount", errorDetails);
                int grandTotal = parseAmount(extractedData, "grandTotal", errorDetails);

                // 이메일 검증
                validateEmail("공급자 이메일", ipEmail, errorDetails);
                validateEmail("공급받는자 이메일", suEmail, errorDetails);

                if (!requiredFieldErrors.isEmpty()) {
                    status = ProcessStatus.UNAPPROVED;
                    errorDetails.addAll(requiredFieldErrors);
                }

                // OCR 성공 후 S3 파일 이동
                String movedFileUrl = awsS3Service.moveFileToFinalFolder(imageUrl, "tax_invoices");

                TaxInvoice taxInvoice;
                TaxInvoiceFile taxFile;

                // TaxInvoice 생성 및 저장
                if (taxInvoiceRepository.findByIssueId(issueId).isEmpty()) {
                    taxInvoice = TaxInvoice.create(
                            issueId, ipId, suId, chargeTotal, taxTotal, grandTotal,
                            erDat, ipBusinessName, suBusinessName, ipName, suName, ipAddress, suAddress,
                            ipEmail, suEmail, member, errorDetails, status
                    );
                    TaxInvoice savedTaxInvoice = taxInvoiceRepository.save(taxInvoice);
                    // TaxInvoiceFile 생성 및 저장
                    taxFile = TaxInvoiceFile.create(savedTaxInvoice, movedFileUrl, file.getContentType(),
                            file.getOriginalFilename(), file.getSize(), LocalDateTime.now());
                    taxInvoiceFileRepository.save(taxFile);
                    savedTaxInvoice.attachFile(taxFile);
                    taxInvoiceRepository.save(savedTaxInvoice);
                }
                else{
                    taxInvoice = taxInvoiceRepository.findByIssueId(issueId).get();
                    taxInvoice.update(
                            issueId, ipId, suId, chargeTotal, taxTotal, grandTotal,
                            erDat, ipBusinessName, suBusinessName, ipName, suName, ipAddress, suAddress,
                            ipEmail, suEmail, member, errorDetails, status
                    );
                    taxFile = taxInvoice.getFile();
                    taxFile.update(taxInvoice, movedFileUrl, file.getContentType(),
                            file.getOriginalFilename(), file.getSize(), LocalDateTime.now());
                    taxInvoice.attachFile(taxFile);
                }

                // OCR 처리 후 해당 이미지의 임시 저장 해제
                if (imageId != null) {
                    imageService.removeFromTemporary(member, List.of(imageId));
                }

                long endTime = System.nanoTime();
                long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
                return TaxInvoiceResponseDTO.Create.from(taxInvoice, imageUrl, extractedData, errorDetails, elapsedTimeMillis);

            } catch (Exception e) {
                System.out.println("[ERROR] OCR 처리 중 예외 발생: " + e.getMessage());
                return TaxInvoiceResponseDTO.Create.error(imageUrl, e.getMessage());
            }
        });
    }

    /**
     * 세금계산서 검색 - provider, consumer 입력 값이 없으면 전체 조회
     * @param provider 공급자
     * @param consumer 공급받는자
     * @param name 관리자는 이름으로 검색 가능
     * @param startDate 시작날짜
     * @param endDate 끝날짜
     * @param period 기간
     * @param status 승인 상태
     * @return 검색 결과
     */
    @Override
    public Page<TaxInvoiceResponseDTO.GetOne> search(MemberEntity member, String provider, String consumer, String name,
                                                     LocalDate startDate, LocalDate endDate, Integer period, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (period != null) {
            endDate = LocalDate.now();
            startDate = endDate.minusMonths(period);
        }
        ProcessStatus processStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                processStatus = ProcessStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.PROCESS_STATUS_INVALID);
            }
        }

        Page<TaxInvoice> taxInvoicePage = taxInvoiceRepository.searchWithFilters(
                provider, consumer, name, member, startDate, endDate, processStatus, pageable);

        return taxInvoicePage.map(TaxInvoiceResponseDTO.GetOne::from);
    }

    /**
     * 세금 계산서 정보 삭제
     * @param taxInvoiceId 삭제할 세금 계산서 ID(PK) 값
     */
    @Override
    public void delete(Long taxInvoiceId) {
        TaxInvoice taxInvoice = taxInvoiceRepository.getById(taxInvoiceId);

        if (taxInvoice.getFile() != null) {
            String fileUrl = taxInvoice.getFile().getFileUrl();
            awsS3Service.deleteFile(fileUrl);
        }

        taxInvoiceRepository.delete(taxInvoiceId);
    }

    /**
     * 세금계산서 정보 엑셀파일로 추출
     * @param taxInvoiceIds
     * @return
     */
    @Override
    public ByteArrayInputStream extractToExcel(List<Long> taxInvoiceIds) {
        List<TaxInvoice> taxInvoiceList = taxInvoiceRepository.findAllById(taxInvoiceIds);
        ByteArrayInputStream result;
        try{
            result = excelMaker.getTaxInvoiceToExcel(taxInvoiceList);
        } catch (IOException e) {
            throw new CustomException(MAKE_EXCEL_FILE_ERROR);
        }
        return result;
    }


    /** 문자열을 TemplateOcrField로 변환하는 메서드 */
    private List<TemplateOcrField> convertToTemplateOcrFields(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            throw new CustomException(ErrorCode.OCR_EMPTY_JSON);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> root = objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> images = (List<Map<String, Object>>) root.get("images");

            if (images == null || images.isEmpty()) {
                throw new CustomException(ErrorCode.OCR_NO_IMAGES);
            }

            List<Map<String, Object>> fields = (List<Map<String, Object>>) images.get(0).get("fields");
            if (fields == null || fields.isEmpty()) {
                throw new CustomException(ErrorCode.OCR_NO_FIELDS);
            }

            return fields.stream()
                    .map(field -> {
                        try {
                            String name = (String) field.get("name");
                            String inferText = (String) field.get("inferText");
                            Map<String, Object> boundingPolyMap = (Map<String, Object>) field.get("boundingPoly");
                            BoundingPoly boundingPoly = objectMapper.convertValue(boundingPolyMap, BoundingPoly.class);
                            return new TemplateOcrField(name, inferText, boundingPoly);
                        } catch (Exception e) {
                            throw new CustomException(ErrorCode.OCR_FIELD_CONVERSION_ERROR);
                        }
                    })
                    .collect(Collectors.toList());

        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.OCR_JSON_PARSING_ERROR);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.OCR_FIELD_CONVERSION_ERROR);
        }
    }

    /** 기본값을 설정하고 값을 가져오는 메서드 */
    private String getOrDefault(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        if (value == null || value.toString().trim().isEmpty()) {
            return defaultValue;
        }
        return value.toString().trim();
    }

    /** 가격 형태의 필드 파싱 */
    private int parseAmount(Map<String, Object> extractedData, String key, List<String> errorDetails) {
        String amountStr = getOrDefault(extractedData, key, "0");

        // 숫자 값 검증
        if (!amountStr.matches("^[0-9,]+$")) {
            errorDetails.add(key + " 숫자 변환 오류: '" + amountStr + "' 값이 숫자가 아닙니다.");
            return -1;
        }

        try {
            return Integer.parseInt(amountStr.replaceAll(",", ""));
        } catch (NumberFormatException e) {
            errorDetails.add(key + " 숫자 변환 오류: " + amountStr);
            return -1;
        }
    }

    /** 필수값 검증 메서드 - 값이 UNKNOWN 또는 빈 문자열이면 오류 메시지를 추가 */
    private void validateRequiredField(String fieldLabel, String fieldValue, List<String> errorDetails) {
        if ("UNKNOWN".equals(fieldValue) || fieldValue.trim().isEmpty()) {
            errorDetails.add("승인번호 인식 오류");
            return;
        }

        // 승인번호 형식 검증 (8-8-8)
        if ("승인번호".equals(fieldLabel)) {
            String approvalPattern = "^\\d{8}-\\d{8}-\\d{8}$";
            if (!Pattern.matches(approvalPattern, fieldValue)) {
                errorDetails.add("승인번호 형식이 올바르지 않습니다: " + fieldValue);
            }
        }

        // 등록번호 형식 검증 (3-2-5)
        if ("공급자 등록번호".equals(fieldLabel) || "공급받는자 등록번호".equals(fieldLabel)) {
            String registrationPattern = "^\\d{3}-\\d{2}-\\d{5}$";
            if (!Pattern.matches(registrationPattern, fieldValue)) {
                errorDetails.add(fieldLabel + " 형식이 올바르지 않습니다.: " + fieldValue);
            }
        }
    }

    /** 이메일 형식 검증 메서드 */
    private void validateEmail(String fieldLabel, String email, List<String> errorDetails) {
        if ("UNKNOWN".equals(email) || email.trim().isEmpty()) {
            return;
        }

        String emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!Pattern.matches(emailPattern, email)) {
            errorDetails.add(fieldLabel + " 형식이 올바르지 않습니다.: " + email);
        }
    }
}
