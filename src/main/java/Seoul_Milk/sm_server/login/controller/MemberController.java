package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.annotation.CurrentMember;
import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import Seoul_Milk.sm_server.login.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "회원")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/myPage")
    @Operation(summary = "마이페이지")
    public SuccessResponse<MemberEntity> myPage(@CurrentMember MemberEntity member) {
        System.out.println("member.getEmployeeId() = " + member.getEmployeeId());
        MemberEntity my = memberService.getMember(member.getEmployeeId());
        return SuccessResponse.ok(my);
    }
}
