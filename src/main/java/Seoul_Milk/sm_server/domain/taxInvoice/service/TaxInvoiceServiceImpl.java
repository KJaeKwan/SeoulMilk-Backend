package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.global.clovaOcr.dto.BoundingPoly;
import Seoul_Milk.sm_server.global.clovaOcr.dto.OcrField;
import Seoul_Milk.sm_server.global.clovaOcr.infrastructure.ClovaOcrApi;
import Seoul_Milk.sm_server.global.clovaOcr.service.OcrDataExtractor;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.global.upload.service.AwsS3Service;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaxInvoiceServiceImpl implements TaxInvoiceService {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final ClovaOcrApi clovaOcrApi;
    private final OcrDataExtractor ocrDataExtractor;
    private final TaxInvoiceRepository taxInvoiceRepository;
    private final AwsS3Service awsS3Service;

    private static final int MAX_REQUESTS_PER_SECOND = 5;  // 초당 최대 5개 요청
    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND, true);

    @Async("ocrTaskExecutor")
    @Override
    public CompletableFuture<TaxInvoiceResponseDTO.Create> processOcrAsync(MultipartFile image, MemberEntity member) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                semaphore.acquire(); // 요청 개수가 5개를 초과하면 자동 대기

                long startTime = System.nanoTime();
                TaxInvoiceResponseDTO.Create response = processOcrSync(image, member);
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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TaxInvoiceResponseDTO.Create processOcrSync(MultipartFile image, MemberEntity member) {
        long startTime = System.nanoTime();
        List<String> errorDetails = new ArrayList<>();
        Map<String, Object> extractedData;

        try {
            // CLOVA OCR API 호출
            String jsonResponse = clovaOcrApi.callApi("POST", image, clovaSecretKey, image.getContentType());
            List<OcrField> ocrFields = convertToOcrFields(jsonResponse);
            extractedData = ocrDataExtractor.extractDataFromOcrFields(ocrFields);

            // 데이터 추출
            String issueId = (String) extractedData.get("approval_number");
            List<String> registrationNumbers = (List<String>) extractedData.get("registration_numbers");
            String totalAmountStr = (String) extractedData.get("total_amount");
            String erDat = (String) extractedData.get("issue_date");
            String ipBusinessName = (String) extractedData.get("supplier_business_name");
            String suBusinessName = (String) extractedData.get("recipient_business_name");
            String ipName = (String) extractedData.get("supplier_name");
            String suName = (String) extractedData.get("recipient_name");

            // 미승인 에러 케이스 추가
            if (issueId == null) {
                errorDetails.add("승인번호 인식 오류");
                issueId = "UNKNOWN";
            }
            if (totalAmountStr == null) {
                errorDetails.add("공급가액 인식 오류");
                totalAmountStr = "UNKNOWN";
            }
            if (erDat == null) {
                errorDetails.add("발행일 인식 오류");
                erDat = "UNKNOWN";
            }

            String ipId = "UNKNOWN";
            String suId = "UNKNOWN";
            if (registrationNumbers != null && !registrationNumbers.isEmpty()) {
                ipId = registrationNumbers.get(0);
            } else {
                errorDetails.add("공급자 사업자 등록번호 인식 오류");
            }

            if (registrationNumbers != null && registrationNumbers.size() > 1) {
                suId = registrationNumbers.get(1);
            } else {
                errorDetails.add("공급받는자 사업자 등록번호 인식 오류");
            }

            int taxTotal;
            if (!totalAmountStr.isEmpty() && !"UNKNOWN".equals(totalAmountStr)) {
                taxTotal = Integer.parseInt(totalAmountStr.replaceAll(",", ""));
            } else {
                errorDetails.add("공급가액 인식 오류");
                taxTotal = -1;
            }

            // TaxInvoice 생성 및 저장
            TaxInvoice taxInvoice = TaxInvoice.create(issueId, ipId, suId, taxTotal, erDat,
                    ipBusinessName, suBusinessName, ipName, suName, member, errorDetails);
            TaxInvoice savedTaxInvoice = taxInvoiceRepository.save(taxInvoice);

            // OCR 추출 성공한 경우 S3 업로드
            String fileUrl = awsS3Service.uploadFile("tax_invoices", image, true);
            TaxInvoiceFile file = TaxInvoiceFile.create(savedTaxInvoice, fileUrl, image.getContentType(), image.getOriginalFilename(), image.getSize(), LocalDateTime.now());
            taxInvoice.attachFile(file);
            taxInvoiceRepository.save(taxInvoice);

            long endTime = System.nanoTime();
            long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            // DTO 반환
            return TaxInvoiceResponseDTO.Create.from(savedTaxInvoice, image.getOriginalFilename(), extractedData, errorDetails, elapsedTimeMillis);

        } catch (Exception e) {
            System.out.println("[ERROR] 세금계산서 처리 중 예외 발생: " + e.getMessage());
            return TaxInvoiceResponseDTO.Create.error(image.getOriginalFilename(), e.getMessage());
        }
    }



    /**
     * 세금계산서 검색 - provider, consumer 입력 값이 없으면 전체 조회
     * @param provider 공급자
     * @param consumer 공급받는자
     * @param employeeId 관리자는 사번으로 검색 가능
     * @param date 특정 날짜
     * @param period 기간
     * @param status 승인 상태
     * @return 검색 결과
     */
    @Override
    public Page<TaxInvoiceResponseDTO.GetOne> search(MemberEntity member, String provider, String consumer, String employeeId,
                                                     LocalDate date, Integer period, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        LocalDate startDate = null;
        LocalDate endDate = null;

        if (date != null) {
            startDate = date;
            endDate = date;
        } else if (period != null) {
            endDate = LocalDate.now();
            startDate = endDate.minusMonths(period);
        }

        ProcessStatus processStatus = null;
        if (status != null) {
            try {
                processStatus = ProcessStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CustomException(ErrorCode.PROCESS_STATUS_INVALID);
            }
        }

        Page<TaxInvoice> taxInvoicePage = taxInvoiceRepository.searchWithFilters(
                provider, consumer, employeeId, member, startDate, endDate, processStatus, pageable);

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


    /** 문자열을 OcrField로 변환하는 메서드 */
    private List<OcrField> convertToOcrFields(String jsonResponse) {
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
                            String inferText = (String) field.get("inferText");
                            Map<String, Object> boundingPolyMap = (Map<String, Object>) field.get("boundingPoly");
                            BoundingPoly boundingPoly = objectMapper.convertValue(boundingPolyMap, BoundingPoly.class);
                            return new OcrField(inferText, boundingPoly);
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

}
