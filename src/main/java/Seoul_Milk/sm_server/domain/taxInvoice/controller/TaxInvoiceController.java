package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceService;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<String> processParallelMultipleImages(@RequestParam("images") List<MultipartFile> images) {
        if (images.isEmpty()) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED);
        }

        long totalStartTime = System.nanoTime();

        // 비동기 OCR 요청 실행
        List<CompletableFuture<Map<String, Object>>> futureResults = images.stream()
                .map(taxInvoiceService::processOcrAsync)
                .toList();

        // allOf로 실행 후 한 번에 처리
        CompletableFuture.allOf(futureResults.toArray(new CompletableFuture[0])).join();

        long totalEndTime = System.nanoTime();
        long totalElapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(totalEndTime - totalStartTime);

        System.out.println("totalElapsedTimeMillis = " + totalElapsedTimeMillis);

        return SuccessResponse.ok("이미지 OCR 처리 후 저장에 성공했습니다.");
    }


    @Operation(summary = "OCR 처리된 모든 이미지 조회")
    @GetMapping
    public SuccessResponse<TaxInvoiceResponseDTO.GetALL> getAllProcessedImages() {
        TaxInvoiceResponseDTO.GetALL result = taxInvoiceService.findAll();
        return SuccessResponse.ok(result);
    }


    @Operation(summary = "OCR 처리된 값 삭제")
    @DeleteMapping("/{taxInvoiceId}")
    public SuccessResponse<String> delete(@PathVariable("taxInvoiceId") Long id) {
        taxInvoiceService.delete(id);
        return SuccessResponse.ok("세금계산서 정보 삭제에 성공했습니다.");
    }

}
