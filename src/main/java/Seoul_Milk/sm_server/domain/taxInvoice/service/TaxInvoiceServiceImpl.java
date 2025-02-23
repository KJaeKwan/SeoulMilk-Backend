package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.global.clovaOcr.infrastructure.ClovaOcrApi;
import Seoul_Milk.sm_server.global.clovaOcr.service.OcrDataExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TaxInvoiceServiceImpl implements TaxInvoiceService {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final ClovaOcrApi clovaOcrApi;
    private final OcrDataExtractor ocrDataExtractor;

    @Override
    @Async("ocrTaskExecutor")
    public CompletableFuture<Map<String, Object>> processOcrAsync(MultipartFile image) {
        long startTime = System.nanoTime();
        Map<String, Object> imageResult = new LinkedHashMap<>();

        try {
            // 실제 CLOVA OCR API 호출
            List<String> ocrResult = clovaOcrApi.callApi("POST", image, clovaSecretKey, image.getContentType());
            if (ocrResult == null || ocrResult.isEmpty()) {
                throw new RuntimeException("OCR API에서 반환된 결과가 없습니다.");
            }

            // OCR 결과를 JSON 형식으로 변환
            String jsonResponse = convertListToJson(ocrResult);
            // 파싱 및 필드 추출
            Map<String, Object> extractedData = ocrDataExtractor.extractHeaderFields(jsonResponse);

            long endTime = System.nanoTime();
            long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            imageResult.put("파일명", image.getOriginalFilename());
            imageResult.put("처리시간(ms)", elapsedTimeMillis);
            imageResult.put("OCR_결과", extractedData);

        } catch (Exception e) {
            imageResult.put("파일명", image.getOriginalFilename());
            imageResult.put("오류", e.getMessage());
        }

        return CompletableFuture.completedFuture(imageResult);
    }

    /**
     * OCR 결과(List<String>)를 간단한 JSON 형태로 변환
     */
    @Override
    public String convertListToJson(List<String> ocrResult) {
        if (ocrResult == null || ocrResult.isEmpty()) {
            return "{}";
        }
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"images\":[{\"fields\":[");
        for (int i = 0; i < ocrResult.size(); i++) {
            jsonBuilder.append("{\"inferText\":\"").append(ocrResult.get(i)).append("\"}");
            if (i < ocrResult.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]}]}");
        return jsonBuilder.toString();
    }
}
