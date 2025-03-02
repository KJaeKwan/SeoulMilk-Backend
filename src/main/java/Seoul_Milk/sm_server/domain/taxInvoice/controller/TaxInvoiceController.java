package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceService;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR API")
public class TaxInvoiceController {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final TaxInvoiceService taxInvoiceService;

    /**
     * 여러 이미지를 병렬로 OCR 처리
     * @param images 입력 이미지 리스트
     * @return 성공 메세지
     */
    @Operation(summary = "여러 개의 이미지를 병렬로 OCR 처리")
    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<String> processParallelMultipleImages(
            @RequestParam("images") List<MultipartFile> images,
            @CurrentMember MemberEntity member
    ) {
        if (images.isEmpty()) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED);
        }

        long totalStartTime = System.nanoTime();

        // 비동기 OCR 요청 실행
        List<CompletableFuture<TaxInvoiceResponseDTO.Create>> futureResults = images.stream()
                .map(image -> taxInvoiceService.processOcrAsync(image, member))
                .toList();

        // allOf로 실행 후 한 번에 처리
        CompletableFuture.allOf(futureResults.toArray(new CompletableFuture[0])).join();

        long totalEndTime = System.nanoTime();
        long totalElapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(totalEndTime - totalStartTime);

        System.out.println("totalElapsedTimeMillis = " + totalElapsedTimeMillis);

        return SuccessResponse.ok("이미지 OCR 처리 후 저장에 성공했습니다.");
    }

    /**
     * 내 업무 조회 - 세금계산서 리스트 반환 (검색 조건에 따라)
     * @param provider 공급자 상호명
     * @param consumer 공급받는자 상호명
     * @param page 페이지 정보
     * @param size 페이지 크기
     * @return 조건에 따른 페이지
     */

    @Operation(
            summary = "내 업무 조회 - 검색",
            description = """
                    - 일반 사원은 본인이 등록한 자료만 조회 가능 (employeeId 사용X)
                    - 관리자는 모든 자료 조회 가능하고, employeeId를 입력하면 특정 사원이 등록한 자료 조회 가능
                    """
    )
    @GetMapping("/search")
    public SuccessResponse<Page<TaxInvoiceResponseDTO.GetOne>> getAllBySearch(
            @CurrentMember MemberEntity member,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String consumer,
            @RequestParam(required = false) String employeeId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<TaxInvoiceResponseDTO.GetOne> result = taxInvoiceService.search(member, provider, consumer, employeeId, page-1, size);
        return SuccessResponse.ok(result);
    }


    /**
     * 세금계산서 ID로 조회하여 삭제
     * @param id 삭제할 TaxInvoice ID
     * @return 삭제 완료 문구
     */
    @Operation(summary = "OCR 처리된 값 삭제")
    @DeleteMapping("/{taxInvoiceId}")
    public SuccessResponse<String> delete(@PathVariable("taxInvoiceId") Long id) {
        taxInvoiceService.delete(id);
        return SuccessResponse.ok("세금계산서 정보 삭제에 성공했습니다.");
    }

}
