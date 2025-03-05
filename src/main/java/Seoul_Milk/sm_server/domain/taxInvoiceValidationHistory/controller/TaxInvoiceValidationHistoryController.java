package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.controller;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceSearchResult;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.request.DeleteTaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service.TaxInvoiceValidationService;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/validation/history")
@RequiredArgsConstructor
@Tag(name = "검증내역 화면 부분<RE_01>")
public class TaxInvoiceValidationHistoryController {
    private final TaxInvoiceValidationService taxInvoiceValidationService;

    @Operation(summary = "<RE_01> 공급자 또는 공급받는자 검색 기능 + 승인, 반려, 검증실패별 조회 api")
    @GetMapping("/search")
    public SuccessResponse<TaxInvoiceSearchResult.GetData> searchAndShowByFilter(
            @CurrentMember MemberEntity memberEntity,
            @RequestParam(value = "poc") String poc,
            @RequestParam(value = "status") @Nullable ProcessStatus processStatus,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        return SuccessResponse.ok(taxInvoiceValidationService.searchByProviderOrConsumer(memberEntity,processStatus, poc, page-1, size));
    }

    @Operation(summary = "<RE_01> 검증 내역 삭제 api")
    @DeleteMapping
    public SuccessResponse<Void> deleteValidationTaxInvoice(
            @CurrentMember MemberEntity memberEntity,
            @RequestBody DeleteTaxInvoiceRequest deleteTaxInvoiceRequest
    ){
        return SuccessResponse.ok(taxInvoiceValidationService.deleteValidationTaxInvoice(memberEntity, deleteTaxInvoiceRequest));
    }

}
