package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.global.clovaOcr.dto.OcrField;
import Seoul_Milk.sm_server.global.clovaOcr.infrastructure.ClovaOcrApi;
import Seoul_Milk.sm_server.global.clovaOcr.service.OcrDataExtractor;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.global.upload.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    @Override
    @Async("ocrTaskExecutor")
    public CompletableFuture<Map<String, Object>> processOcrAsync(MultipartFile image) {
        long startTime = System.nanoTime();
        Map<String, Object> imageResult = new LinkedHashMap<>();

        try {
            // 실제 CLOVA OCR API 호출
            List<String> ocrResult = clovaOcrApi.callApi("POST", image, clovaSecretKey, image.getContentType());
            if (ocrResult == null || ocrResult.isEmpty()) {
                throw new CustomException(ErrorCode.OCR_NO_RESULT);
            }

            // OCR 결과를 DTO 리스트로 변환
            List<OcrField> ocrFields = convertToOcrFields(ocrResult);

            // 데이터 추출
            Map<String, Object> extractedData = extractDataFromOcrFields(ocrFields);

            // DB에 저장
            String issueId = (String) extractedData.get("approval_number");

            @SuppressWarnings("unchecked") // 캐스팅에 대한 경고 무시
            List<String> registrationNumbers = (List<String>) extractedData.get("registration_numbers");
            if (registrationNumbers == null || registrationNumbers.size() < 2) {
                throw new CustomException(ErrorCode.INSUFFICIENT_REGISTRATION_NUMBERS);
            }
            String ipId = registrationNumbers.get(0);
            String suId = registrationNumbers.get(1);

            // 총 공급가액 -> 문자열을 정수로 변환
            String totalAmountStr = (String) extractedData.get("total_amount");
            int taxTotal = 0;
            if (totalAmountStr != null && !totalAmountStr.isEmpty()) {
                taxTotal = Integer.parseInt(totalAmountStr.replaceAll(",", ""));
            }

            String erDat = (String) extractedData.get("issue_date");

            // TaxInvoice 엔티티 생성 및 DB 저장
            TaxInvoice taxInvoice = TaxInvoice.create(issueId, ipId, suId, taxTotal, erDat);
            TaxInvoice savedTaxInvoice = taxInvoiceRepository.save(taxInvoice);

            // OCR 추출에 성공한 이미지에 대해 S3 업로드
            String fileUrl = awsS3Service.uploadFile("tax_invoices", image, true);

            TaxInvoiceFile file = TaxInvoiceFile.create(savedTaxInvoice, fileUrl, image.getContentType(), image.getOriginalFilename(), image.getSize(), LocalDateTime.now());
            taxInvoice.attachFile(file);
            taxInvoiceRepository.save(taxInvoice);

            long endTime = System.nanoTime();
            long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            imageResult.put("파일명", image.getOriginalFilename());
            imageResult.put("처리시간", elapsedTimeMillis);
            imageResult.put("저장된_데이터", taxInvoice);

        } catch (Exception e) {
            imageResult.put("파일명", image.getOriginalFilename());
            imageResult.put("오류", e.getMessage());
        }

        return CompletableFuture.completedFuture(imageResult);
    }

    /**
     * 세금 계산서 정보 리스트로 조회
     * @return
     */
    @Override
    public TaxInvoiceResponseDTO.GetALL findAll() {
        List<TaxInvoice> taxInvoices = taxInvoiceRepository.findAll();
        return TaxInvoiceResponseDTO.GetALL.from(taxInvoices);
    }

    /**
     * 세금 계산서 정보 삭제
     * @param taxInvoiceId 삭제할 세금 계산서 ID(PK) 값
     */
    @Override
    public void delete(Long taxInvoiceId) {
        taxInvoiceRepository.getById(taxInvoiceId);
        taxInvoiceRepository.delete(taxInvoiceId);
    }


    /**
     * OCR에서 필요한 데이터를 추출하는 메서드
     */
    private Map<String, Object> extractDataFromOcrFields(List<OcrField> ocrFields) {
        Map<String, Object> extractedData = new LinkedHashMap<>();
        List<String> registrationNumbers = new ArrayList<>();

        Pattern pricePattern = Pattern.compile("^\\d{1,3}(,\\d{3})*$");
        Pattern datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        Pattern approvalNumberPattern = Pattern.compile("\\d{8}-\\d{8}-\\d{8}");
        Pattern registrationNumberPattern = Pattern.compile("\\d{3}-\\d{2}-\\d{5}");

        boolean foundIssueDate = false;
        boolean foundTotalAmount = false;
        boolean foundTaxAmount = false;

        for (int i = 0; i < ocrFields.size(); i++) {
            OcrField currentField = ocrFields.get(i);
            String currentText = currentField.getInferText().replace(" ", "").trim();

            // 승인번호
            if (approvalNumberPattern.matcher(currentText).matches()) {
                extractedData.put("approval_number", currentText);
                continue;
            }

            // 등록번호
            if (registrationNumberPattern.matcher(currentText).matches()) {
                registrationNumbers.add(currentText);
                continue;
            }

            // 작성일자
            Matcher dateMatcher = datePattern.matcher(currentText);
            if (dateMatcher.matches() && !foundIssueDate) {
                extractedData.put("issue_date", currentText);
                foundIssueDate = true;
                continue;
            }

            // 이메일
            Matcher emailMatcher = emailPattern.matcher(currentText);
            if (emailMatcher.matches()) {
                extractedData.put("email", currentText);
                continue;
            }

            // 공급가액
            if (currentText.equals("공급가액") && !foundTotalAmount) {
                for (int j = i + 1; j < ocrFields.size(); j++) {
                    if (isValidPrice(ocrFields.get(j))) {
                        extractedData.put("total_amount", ocrFields.get(j).getInferText());
                        foundTotalAmount = true;
                        break;
                    }
                }
                continue;
            }

            // 세액
            if (currentText.equals("세액") && !foundTaxAmount) {
                for (int j = i + 1; j < ocrFields.size(); j++) {
                    if (isValidPrice(ocrFields.get(j))) {
                        extractedData.put("tax_amount", ocrFields.get(j).getInferText());
                        foundTaxAmount = true;
                        break;
                    }
                }
                continue;
            }
        }

        if (!registrationNumbers.isEmpty()) {
            extractedData.put("registration_numbers", registrationNumbers);
        }

        return extractedData;
    }

    /** 문자열을 OcrField로 변환하는 메서드 */
    private List<OcrField> convertToOcrFields(List<String> ocrResult) {
        if (ocrResult == null || ocrResult.isEmpty()) {
            return Collections.emptyList();
        }

        return ocrResult.stream()
                .map(text -> new OcrField(text, null)) // boundingPoly가 없으므로 null 처리
                .collect(Collectors.toList());
    }

    /** 가격 형식인지 검증하는 메서드 */
    private boolean isValidPrice(OcrField field) {
        if (field == null || field.getInferText() == null) {
            return false;
        }

        String priceText = field.getInferText().replaceAll(",", "").trim();

        // 숫자로만 이루어졌는지 검사
        if (!priceText.matches("^\\d+$")) {
            return false;
        }

        // 숫자로 변환 후 최소 10,000 이상인지 확인
        try {
            int value = Integer.parseInt(priceText);
            return value >= 10000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
