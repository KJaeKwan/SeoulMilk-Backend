package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.JoinDTO;
import Seoul_Milk.sm_server.login.service.JoinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Tag(name = "회원")
public class JoinController {
    private final JoinService joinService;
    @PostMapping("/join")
    @Operation(summary = "회원가입 API")
    public SuccessResponse<?> joinProcess(JoinDTO joinDTO){
        joinService.joinProcess(joinDTO);
        return SuccessResponse.ok(null);
    }
}
