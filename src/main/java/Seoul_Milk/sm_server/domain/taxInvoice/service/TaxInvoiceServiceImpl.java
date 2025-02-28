package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    public CompletableFuture<Map<String, Object>> processOcrAsync(MultipartFile image, MemberEntity member) {
        long startTime = System.nanoTime();
        Map<String, Object> imageResult = new LinkedHashMap<>();

        try {
            // CLOVA OCR API 호출 (JSON 응답 받음)
            String jsonResponse = clovaOcrApi.callApi("POST", image, clovaSecretKey, image.getContentType());

            // OCR 결과를 DTO 리스트로 변환
            List<OcrField> ocrFields = convertToOcrFields(jsonResponse);

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
            String supplierBusinessName = (String) extractedData.get("supplier_business_name");
            String recipientBusinessName = (String) extractedData.get("recipient_business_name");
            String supplierName = (String) extractedData.get("supplier_name");
            String recipientName = (String) extractedData.get("recipient_name");

            // TaxInvoice 엔티티 생성 및 DB 저장
            TaxInvoice taxInvoice = TaxInvoice.create(issueId, ipId, suId, taxTotal, erDat,
                    supplierBusinessName, recipientBusinessName,
                    supplierName, recipientName, member);
            TaxInvoice savedTaxInvoice = taxInvoiceRepository.save(taxInvoice);

            // OCR 추출에 성공한 이미지에 대해 S3 업로드
            String fileUrl = awsS3Service.uploadFile("tax_invoices", image, true);

            TaxInvoiceFile file = TaxInvoiceFile.create(savedTaxInvoice, fileUrl, image.getContentType(), image.getOriginalFilename(), image.getSize(), LocalDateTime.now());
            taxInvoice.attachFile(file);
            taxInvoiceRepository.save(taxInvoice);

            long endTime = System.nanoTime();
            long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            imageResult.put("추출된 데이터", extractedData);
            imageResult.put("파일명", image.getOriginalFilename());
            imageResult.put("처리시간", elapsedTimeMillis);
            imageResult.put("저장된_데이터", taxInvoice);

//            System.out.println("최종 결과 확인: " + imageResult);

        } catch (Exception e) {
            imageResult.put("파일명", image.getOriginalFilename());
            imageResult.put("오류", e.getMessage());
        }

        return CompletableFuture.completedFuture(imageResult);
    }

    /**
     * 세금계산서 검색 - provider, consumer 입력 값이 없으면 전체 조회
     * @param provider 공급자
     * @param consumer 공급받는자
     * @return 검색 결과
     */
    @Override
    public Page<TaxInvoiceResponseDTO.GetOne> search(MemberEntity member, String provider, String consumer, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<TaxInvoice> taxInvoicePage;
        if (provider != null && !provider.isEmpty() && consumer != null && !consumer.isEmpty()) {
            taxInvoicePage = taxInvoiceRepository.findByProviderAndConsumer(provider, consumer, pageable); // 공급자 + 공급받는자 로 검색
        } else if (provider != null && !provider.isEmpty()) {
            taxInvoicePage = taxInvoiceRepository.findByProvider(provider, pageable); // 공급자 로만 검색
        } else if (consumer != null && !consumer.isEmpty()) {
            taxInvoicePage = taxInvoiceRepository.findByConsumer(consumer, pageable);// 공급받는자 로만 검색
        } else {
            taxInvoicePage = taxInvoiceRepository.findAll(pageable); // 전체 조회
        }

        return taxInvoicePage.map(TaxInvoiceResponseDTO.GetOne::from);
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
