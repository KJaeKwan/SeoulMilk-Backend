package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.VerifyPwDTO;
import Seoul_Milk.sm_server.login.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.login.dto.request.UpdateRoleDTO;
import Seoul_Milk.sm_server.login.dto.response.MemberResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import Seoul_Milk.sm_server.login.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원")
public class MemberController {

    private final MemberService memberService;

    /**
     * 마이페이지 조회 API
     * @param member 현재 로그인 중인 사용자
     * @return 사용자 정보
     */
    @GetMapping("/myPage")
    @Operation(summary = "마이페이지")
    public SuccessResponse<MemberEntity> myPage(@CurrentMember MemberEntity member) {
        MemberEntity my = memberService.getMember(member.getEmployeeId());
        return SuccessResponse.ok(my);
    }

    /**
     * 비밀번호를 변경하는 API
     * @param member 현재 로그인 중인 사용자
     * @param request 새 비밀번호, 비밀번호 검증을 입력
     * @return 응답 메세지
     */
    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경")
    public SuccessResponse<String> updatePassword(
            @CurrentMember MemberEntity member,
            @Valid @RequestBody UpdatePwDTO request) {
        memberService.updatePw(member.getId(), request);
        return SuccessResponse.ok("비밀번호 변경에 성공했습니다.");
    }

    /**
     * <내 업무 조회> 에 사용될 비밀번호 검증 API
     * @param member 현재 로그인 중인 사용자
     * @param request 사용자의 비밀번호와 비교할 비밀번호 입력
     * @return 검증 결과 반환 (성공시 true)
     */
    @PostMapping("/password/verify")
    @Operation(summary = "비밀번호 확인")
    public SuccessResponse<Boolean> verifyPassword(
            @CurrentMember MemberEntity member,
            @Valid @RequestBody VerifyPwDTO request) {
            boolean isMatch = memberService.verifyPassword(member.getId(), request);
        return SuccessResponse.ok(isMatch);
    }

    /**
     * [임시] 권한 변경 API
     * @param request 사용자 ID, 변경할 권한 입력
     * @return 사용자 정보 반환
     */
    @PatchMapping("/{memberId}/role/test")
    @Operation(summary = "[통신에 사용X] 관리자 권한 없이 권한 변경 - 초기에 관리자 등록을 위해 만들어 놓았습니다.")
    public SuccessResponse<MemberResponse> testUpdateRole(
            @Valid @RequestBody UpdateRoleDTO request
    ) {
        MemberResponse result = memberService.testUpdateRole(request);
        return SuccessResponse.ok(result);
    }

    /**
     * 권한 변경 API
     */
    @PatchMapping("/{memberId}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "사용자 권한 변경")
    public SuccessResponse<MemberResponse> updateRole(
            @CurrentMember MemberEntity member,
            @Valid @RequestBody UpdateRoleDTO request
    ) {
        MemberResponse result = memberService.updateRole(member.getId(), request);
        return SuccessResponse.ok(result);
    }
}
