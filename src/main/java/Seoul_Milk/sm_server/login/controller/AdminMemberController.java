package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.request.RegisterDTO;
import Seoul_Milk.sm_server.login.dto.request.UpdateRoleDTO;
import Seoul_Milk.sm_server.login.dto.response.MemberResponse;
import Seoul_Milk.sm_server.login.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@Tag(name = "관리자 회원 관리 API - 관리자만 접근 가능")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminMemberController {

    private final MemberService memberService;

    /**
     * 사원 추가 API
     */
    @PostMapping("/register/tmp")
    @Operation(summary = "사원 등록 API", description = "사번, 사원명, 권한을 입력받습니다.")
    public SuccessResponse<MemberResponse> tmpRegisterMember(@RequestBody RegisterDTO registerDTO) {
        MemberResponse result = memberService.register(registerDTO);
        return SuccessResponse.ok(result);
    }

    /**
     * 권한 변경 API
     */
    @PatchMapping("/{memberId}/role")
    @Operation(summary = "사용자 권한 변경")
    public SuccessResponse<MemberResponse> updateRole(
            @Valid @RequestBody UpdateRoleDTO request
    ) {
        MemberResponse result = memberService.updateRole(request);
        return SuccessResponse.ok(result);
    }

    /**
     * 사번 존재 검증 API
     */
    @GetMapping("/exists/{employeeId}")
    @Operation(summary = "사번 존재 여부 확인", description = "해당 사번이 존재하는지 여부를 반환합니다.")
    public SuccessResponse<Boolean> checkEmployeeNumberExists(@PathVariable String employeeId) {
        Boolean exists = memberService.existsByEmployeeId(employeeId);
        return SuccessResponse.ok(exists);
    }

}
