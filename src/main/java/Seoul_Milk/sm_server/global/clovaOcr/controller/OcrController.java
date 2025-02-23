package Seoul_Milk.sm_server.global.clovaOcr.controller;

import Seoul_Milk.sm_server.global.clovaOcr.infrastructure.ClovaOcrApi;
import Seoul_Milk.sm_server.global.clovaOcr.service.OcrDataExtractor;
import Seoul_Milk.sm_server.global.clovaOcr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR API")
public class OcrController {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final ClovaOcrApi clovaOcrApi;
    private final OcrDataExtractor ocrDataExtractor;
    private final OcrService ocrService;

    /**
     * 클라이언트가 Multipart 이미지를 업로드하면 OCR 실행
     * @param file 업로드된 이미지 파일
     */
    @Operation(summary = "이미지 OCR 에디터")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadOcr(@RequestParam(value = "file", required = false) MultipartFile file) {
        long startTime = System.nanoTime();

        List<String> result = clovaOcrApi.callApi("POST", file, clovaSecretKey, file.getContentType());

        long endTime = System.nanoTime(); // 종료 시간 측정
        double elapsedTime = (endTime - startTime) / 1_000_000.0; // 밀리초(ms) 단위 변환
        System.out.println("OCR API 응답 시간: " + elapsedTime + " ms");

        return ResponseEntity.ok(result);
    }




    /**
     * OCR 실행 후, 매핑된 데이터를 확인하는 API
     * @param file 이미지 파일 (전자계산서)
     * @return OCR 결과 (필드 매핑된 JSON)
     */
    @Operation(summary = "이미지를 OCR 처리 후 응답 JSON 반환")
    @PostMapping(value = "/test-mapping", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> testOcrMapping(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 없습니다.");
        }
        List<String> ocrResult = clovaOcrApi.callApi("POST", file, clovaSecretKey, file.getContentType());

        if (ocrResult == null || ocrResult.isEmpty()) {
            System.err.println("OCR API에서 반환된 결과가 없습니다.");
            return ResponseEntity.status(500).body("OCR 결과가 없습니다.");
        }

        String ocrJsonResponse = ocrService.convertListToJson(ocrResult);
        System.out.println("OCR JSON 변환 결과: " + ocrJsonResponse);

        try {
            Map<String, Object> headerData = ocrDataExtractor.extractHeaderFields(ocrJsonResponse);

            // 매핑된 데이터를 JSON 응답으로 반환
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            return ResponseEntity.ok().headers(headers).body(Map.of(
                    "headerData", headerData
            ));
        } catch (Exception e) {
            System.err.println("OCR 데이터 매핑 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body("OCR 데이터 매핑 중 오류 발생: " + e.getMessage());
        }
    }


    /**
     * 여러 개의 이미지를 OCR 처리하는 API
     */
    @Operation(summary = "여러 개의 이미지를 OCR 처리")
    @PostMapping(value = "/process-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> processMultipleImages(@RequestParam("images") List<MultipartFile> images) {
        if (images.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "이미지가 업로드되지 않았습니다."));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();

        long totalStartTime = System.nanoTime(); // 전체 시작 시간

        for (MultipartFile image : images) {
            Map<String, Object> imageResult = new LinkedHashMap<>();

            try {
                long startTime = System.nanoTime(); // 개별 이미지 시작 시간

                // OCR API 요청
                List<String> ocrResult = clovaOcrApi.callApi("POST", image, clovaSecretKey, image.getContentType());

                if (ocrResult == null || ocrResult.isEmpty()) {
                    throw new RuntimeException("OCR API에서 반환된 결과가 없습니다.");
                }

                String jsonResponse = ocrService.convertListToJson(ocrResult);
                Map<String, Object> extractedData = ocrDataExtractor.extractHeaderFields(jsonResponse);

                long endTime = System.nanoTime(); // 개별 이미지 종료 시간
                long elapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

                imageResult.put("파일명", image.getOriginalFilename());
                imageResult.put("처리시간(ms)", elapsedTimeMillis);
                imageResult.put("OCR_결과", extractedData);

            } catch (Exception e) {
                imageResult.put("파일명", image.getOriginalFilename());
                imageResult.put("오류", e.getMessage());
            }

            results.add(imageResult);
        }

        long totalEndTime = System.nanoTime(); // 전체 종료 시간
        long totalElapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(totalEndTime - totalStartTime);

        response.put("총 이미지 개수", images.size());
        response.put("총 소요 시간(ms)", totalElapsedTimeMillis);
        response.put("처리 결과", results);

        return ResponseEntity.ok(response);
    }


    /**
     * 여러 이미지를 병렬로 처리
     */
    @Operation(summary = "여러 개의 이미지를 병렬로 OCR 처리")
    @PostMapping(value = "/parallel-process-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> processParallelMultipleImages(@RequestParam("images") List<MultipartFile> images) {
        if (images.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "이미지가 업로드되지 않았습니다."));
        }

        long totalStartTime = System.nanoTime();

        // 비동기 OCR 요청 실행
        List<CompletableFuture<Map<String, Object>>> futureResults = images.stream()
                .map(ocrService::processOcrAsync)
                .collect(Collectors.toList());

        // allOf로 실행 후 한 번에 처리
        CompletableFuture.allOf(futureResults.toArray(new CompletableFuture[0])).join();

        // 병렬 실행 후 결과 수집
        List<Map<String, Object>> results = futureResults.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        long totalEndTime = System.nanoTime();
        long totalElapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(totalEndTime - totalStartTime);

        // 최종 응답 데이터 구성
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("총 이미지 개수", images.size());
        response.put("총 소요 시간(ms)", totalElapsedTimeMillis);
        response.put("처리 결과", results);

        return ResponseEntity.ok(response);
    }

}
