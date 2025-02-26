package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.request.TestUpdateRoleDTO;
import Seoul_Milk.sm_server.login.dto.response.MemberResponse;
import Seoul_Milk.sm_server.login.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import Seoul_Milk.sm_server.login.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/myPage")
    @Operation(summary = "마이페이지")
    public SuccessResponse<MemberEntity> myPage(@CurrentMember MemberEntity member) {
        MemberEntity my = memberService.getMember(member.getEmployeeId());
        return SuccessResponse.ok(my);
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경")
    public SuccessResponse<String> updatePassword(
            @CurrentMember MemberEntity member,
            @Valid @RequestBody UpdatePwDTO request) {
        memberService.updatePw(member.getId(), request);
        return SuccessResponse.ok("비밀번호 변경에 성공했습니다.");
    }

    /**
     * [임시] 권한 변경 API
     * @param request 사용자 ID, 변경할 권한 입력
     * @return 사용자 정보 반환
     */
    @PatchMapping("/{memberId}/role/test")
    @Operation(summary = "[통신에 사용X] 관리자 권한 없이 권한 변경 - 초기에 관리자 등록을 위해 만들어 놓았습니다.")
    public SuccessResponse<MemberResponse> testUpdateRole(
            @Valid @RequestBody TestUpdateRoleDTO request
    ) {
        MemberResponse result = memberService.testUpdateRole(request);
        return SuccessResponse.ok(result);
    }
}
