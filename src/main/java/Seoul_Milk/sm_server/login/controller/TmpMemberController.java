package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.request.RegisterDTO;
import Seoul_Milk.sm_server.login.dto.response.MemberResponse;
import Seoul_Milk.sm_server.login.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tmp")
@RequiredArgsConstructor
@Tag(name = "[통신에 사용X] 초기에 생성을 위한 API")
public class TmpMemberController {

    private final MemberService memberService;

    /**
     * 초기 사원 추가 API
     */
    @PostMapping("/register")
    @Operation(summary = "[임시] 사원 등록 API - 처음 관리자 생성에만 사용", description = "사번, 사원명, 권한(ROLE_NORMAL, ROLE_ADMIN)을 입력받습니다.")
    public SuccessResponse<MemberResponse> tmpRegisterMember(@RequestBody RegisterDTO registerDTO) {
        MemberResponse result = memberService.register(registerDTO);
        return SuccessResponse.ok(result);
    }
}
