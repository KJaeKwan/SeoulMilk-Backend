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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
            Map<String, Object> extractedData = ocrDataExtractor.extractDataFromOcrFields(ocrFields);

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
     * @return 세금 계산서 리스트 반환
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


    /** 문자열을 OcrField로 변환하는 메서드 */
    private List<OcrField> convertToOcrFields(List<String> ocrResult) {
        if (ocrResult == null || ocrResult.isEmpty()) {
            return Collections.emptyList();
        }

        return ocrResult.stream()
                .map(text -> new OcrField(text, null)) // boundingPoly가 없으므로 null 처리
                .collect(Collectors.toList());
    }

}
