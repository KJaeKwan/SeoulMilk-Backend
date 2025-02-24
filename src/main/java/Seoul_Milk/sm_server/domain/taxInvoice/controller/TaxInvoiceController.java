package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR API")
public class TaxInvoiceController {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final TaxInvoiceService taxInvoiceService;

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
                .map(taxInvoiceService::processOcrAsync)
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
