package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import Seoul_Milk.sm_server.domain.image.entity.Image;
import Seoul_Milk.sm_server.domain.image.service.ImageService;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TaxInvoiceService;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR API")
public class TaxInvoiceController {

    @Value("${clova.ocr.secret-key}")
    private String clovaSecretKey;

    private final TaxInvoiceService taxInvoiceService;
    private final ImageService imageService;

    /**
     * 여러 이미지를 병렬로 OCR 처리
     * @param images 입력 이미지 리스트
     * @param member 로그인 유저
     * @return 성공 메세지
     */
    @Operation(
            summary = "로컬 업로드 + 임시 저장 이미지를 병렬로 OCR 처리",
            description = """
                    - 로컬 업로드 이미지만 넣어도 동작
                    - 임시 저장 이미지만 넣어도 동작
                    - 로그인 유저의 임시 저장 이미지는 자동으로 전부 들어간다
                    - 두 경우를 합친 이미지가 0개이면 에러 발생
                    """)
    @RequestBody(content = @Content(
            encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE))) // request 내부에 Content Type 설정 (Swagger)
    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<List<TaxInvoiceResponseDTO.Create>> processParallelMultipleImages(
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "data", required = false) List<Long> tempImageIds,
            @CurrentMember MemberEntity member
    ) {
        long totalStartTime = System.nanoTime();

        // 로컬 업로드된 이미지 리스트
        List<MultipartFile> localFiles = (images != null) ? images : new ArrayList<>();

        // 임시 저장된 이미지 조회
        List<Image> tempImages = new ArrayList<>();
        if (tempImageIds != null && !tempImageIds.isEmpty()) {
            tempImages = imageService.getTempImagesByIds(member, tempImageIds);
        }

        // 처리할 이미지가 없는 경우 예외 발생
        if (localFiles.isEmpty() && tempImages.isEmpty()) {
            throw new CustomException(ErrorCode.UPLOAD_FAILED);
        }

        // 비동기 OCR 요청 실행
        List<CompletableFuture<TaxInvoiceResponseDTO.Create>> futureLocalResults = localFiles.stream()
                .map(file -> taxInvoiceService.processTemplateOcrAsync(file, member))
                .toList();
        List<CompletableFuture<TaxInvoiceResponseDTO.Create>> futureTempResults = tempImages.stream()
                .map(image -> {
                    System.out.println("[DEBUG] OCR 요청: 임시 저장 이미지 ID -> " + image.getId());
                    return taxInvoiceService.processTemplateOcrSync(image.getImageUrl(), member, image.getId())
                            .thenApply(result -> {
                                System.out.println("[DEBUG] OCR 완료: " + image.getId());
                                return result;
                            });
                })
                .toList();

        // allOf로 실행 후 한 번에 처리
        List<TaxInvoiceResponseDTO.Create> result = Stream.concat(futureLocalResults.stream(), futureTempResults.stream())
                .map(CompletableFuture::join)
                .filter(response -> response != null && response.extractedData() != null && !response.extractedData().isEmpty()) // OCR 실패한 데이터 제거
                .toList();

        long totalEndTime = System.nanoTime();
        long totalElapsedTimeMillis = TimeUnit.NANOSECONDS.toMillis(totalEndTime - totalStartTime);

        System.out.println("totalElapsedTimeMillis = " + totalElapsedTimeMillis);

        return SuccessResponse.ok(result);
    }

    /**
     * 내 업무 조회 - 세금계산서 리스트 반환 (검색 조건에 따라)
     * @param provider 공급자 상호명
     * @param consumer 공급받는자 상호명
     * @param date 특정 날짜
     * @param period 기간
     * @param status 승인 상태
     * @param page 페이지 정보
     * @param size 페이지 크기
     * @return 조건에 따른 페이지
     */

    @Operation(
            summary = "내 업무 조회 - 검색",
            description = """
                    - 일반 사원은 본인이 등록한 자료만 조회 가능 (employeeId 사용X)
                    - 관리자는 모든 자료 조회 가능하고, employeeId를 입력하면 특정 사원이 등록한 자료 조회 가능
                    - 특정 날짜 또는 최근 1, 3, 6개월 내 데이터 조회 가능
                    - 승인 상태(승인, 반려, 미승인) 필터링 가능
                    """
    )
    @GetMapping("/search")
    public SuccessResponse<Page<TaxInvoiceResponseDTO.GetOne>> getAllBySearch(
            @CurrentMember MemberEntity member,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String consumer,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Integer period,
            @RequestParam(required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<TaxInvoiceResponseDTO.GetOne> result = taxInvoiceService.search(member, provider, consumer, employeeId, date, period, status, page-1, size);
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
