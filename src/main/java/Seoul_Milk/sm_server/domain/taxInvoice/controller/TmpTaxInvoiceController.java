package Seoul_Milk.sm_server.domain.taxInvoice.controller;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.service.TmpTaxInvoiceService;
import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.response.SuccessResponse;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tax/tmp")
@RequiredArgsConstructor
@Tag(name = "임시 저장 세금계산서 API")
public class TmpTaxInvoiceController {

    private final TmpTaxInvoiceService tmpTaxInvoiceService;


    /**
     * 특정 멤버의 임시 저장된 세금계산서 전체 조회
     * @param member 로그인 유저
     * @return 응답 계산서 DTO로 리스트 반환
     */
    @Operation(summary = "임시 저장된 세금계산서 전체 조회")
    @GetMapping
    public SuccessResponse<List<TaxInvoiceResponseDTO.GetOne>> getAllTemporary(
            @CurrentMember MemberEntity member) {
        List<TaxInvoiceResponseDTO.GetOne> result = tmpTaxInvoiceService.getAll(member);
        return SuccessResponse.ok(result);
    }


    /**
     * 특정 ID 리스트를 찾아 임시 저장으로 등록
     * @param taxInvoiceIds 임시 저장할 세금계산서 ID 리스트
     * @param member 로그인 유저
     * @return 성공 메세지
     */
    @Operation(summary = "특정 세금계산서를 임시 저장 상태로 변경")
    @PatchMapping("/mark")
    public SuccessResponse<String> markAsTemporary(
            @RequestBody List<Long> taxInvoiceIds,
            @CurrentMember MemberEntity member) {
        tmpTaxInvoiceService.markAsTemporary(taxInvoiceIds, member);
        return SuccessResponse.ok("선택한 세금계산서가 임시 저장으로 설정되었습니다.");
    }


    /**
     * 특정 ID 리스트의 세금계산서를 임시 저장에서 해제
     * @param taxInvoiceIds 임시 저장 해제할 계산서 ID 리스트
     * @param member 로그인 유저
     * @return 해제 성공 메세지
     */
    @Operation(summary = "특정 세금계산서를 임시 저장 해제")
    @PatchMapping("/unmark")
    public SuccessResponse<String> removeFromTemporary(
            @RequestBody List<Long> taxInvoiceIds,
            @CurrentMember MemberEntity member) {
        tmpTaxInvoiceService.removeFromTemporary(taxInvoiceIds, member);
        return SuccessResponse.ok("선택한 세금계산서의 임시 저장 상태가 해제되었습니다.");
    }
}
