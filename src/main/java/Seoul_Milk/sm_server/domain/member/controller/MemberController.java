package Seoul_Milk.sm_server.domain.member.controller;

import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.response.SuccessResponse;
import Seoul_Milk.sm_server.domain.member.dto.request.VerifyPwDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.ResetPwDTO;
import Seoul_Milk.sm_server.domain.member.dto.request.UpdatePwDTO;
import Seoul_Milk.sm_server.domain.member.dto.response.MemberResponse;
import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
    @Operation(summary = "<MY_01> 마이페이지")
    public SuccessResponse<MemberResponse> myPage(@CurrentMember MemberEntity member) {
        log.info("myPage 호출 - 현재 로그인 유저: {}", member);
        MemberResponse my = memberService.getMember(member.getEmployeeId());
        return SuccessResponse.ok(my);
    }

    /**
     * 비밀번호 초기화 API
     * @param member 현재 로그인 중인 사용자
     * @param request 새 비밀번호, 비밀번호 검증을 입력
     * @return 응답 메세지
     */
    @PatchMapping("/users/password/reset")
    @Operation(summary = "<LO_01> 비밀번호 초기화 (이메일 인증 후)")
    public SuccessResponse<String> resetPassword(
            @CurrentMember MemberEntity member,
            @Valid @RequestBody ResetPwDTO request) {
        memberService.resetPw(member.getId(), request);
        return SuccessResponse.ok("비밀번호가 초기화되었습니다.");
    }

    /**
     * 비밀번호 변경 API
     * @param member 현재 로그인 중인 사용자
     * @param request 현재 비밀번호, 새 비밀번호, 새 비밀번호 검증을 입력
     * @return 응답 메세지
     */
    @PatchMapping("/password")
    @Operation(summary = "<MY_01> 로그인 페이지에서 비밀번호 변경")
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
    @Operation(summary = "<MY_00> 비밀번호 확인")
    public SuccessResponse<Boolean> verifyPassword(
            @CurrentMember MemberEntity member,
            @Valid @RequestBody VerifyPwDTO request) {
            boolean isMatch = memberService.verifyPassword(member.getId(), request);
        return SuccessResponse.ok(isMatch);
    }

    /**
     * 사번 존재 검증 API
     * @param employeeId 검증할 사번
     * @return 사번 존재 여부 true, false로 반환
     */
    @GetMapping("/exists/{employeeId}")
    @Operation(summary = "사번 존재 여부 확인", description = "해당 사번이 존재하는지 여부를 반환합니다.")
    public SuccessResponse<Boolean> checkEmployeeNumberExists(@PathVariable String employeeId) {
        Boolean exists = memberService.existsByEmployeeId(employeeId);
        return SuccessResponse.ok(exists);
    }

}
