package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.JoinDTO;
import Seoul_Milk.sm_server.login.service.JoinService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class JoinController {
    private final JoinService joinService;

    @PostMapping("/join")
    @Operation(summary = "사원 추가 메서드(테스트용)", description = "아직 관리자페이지 사원추가 메서드가 구현되지 않아서 임시로 만들어둔 API입니다. (프론트는 신경 안 써도 됩니다)")
    public SuccessResponse<?> joinProcess(@RequestBody JoinDTO joinDTO){
        joinService.joinProcess(joinDTO);
        return SuccessResponse.ok(null);
    }
}
