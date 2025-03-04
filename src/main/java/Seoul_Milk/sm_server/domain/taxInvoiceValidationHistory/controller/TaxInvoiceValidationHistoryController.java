package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.controller;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service.TaxInvoiceValidationService;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validation/history")
@RequiredArgsConstructor
@Tag(name = "검증내역 화면 부분<RE_01>")
public class TaxInvoiceValidationHistoryController {
    private final TaxInvoiceValidationService taxInvoiceValidationService;

    @Operation(summary = "<RE_01> 검증내역 조회 api",
            description = """
                    - 전체검색 원하면 option 파라미터에 null값(아무값도 x)
                    - 승인데이터 검색 원하면 option 파라미터에 APPROVED
                    - 반려데이터 검색 원하면 option 파라미터에 REJECTED
                    - 검증실패는 검색 원하면 option 파라미터에 OCR_ERROR(필드 값 안정해짐)
                    """)
    @GetMapping
    public SuccessResponse<Page<GetHistoryData>> showTaxInvoiceValidationData(
            @CurrentMember MemberEntity memberEntity,
            @RequestParam(value = "option") @Nullable ProcessStatus processStatus,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        return SuccessResponse.ok(taxInvoiceValidationService.showTaxInvoice(processStatus, memberEntity, page-1, size));
    }
}
