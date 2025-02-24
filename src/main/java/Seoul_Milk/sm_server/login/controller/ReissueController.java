package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.service.ReissueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "토큰 재발급")
public class ReissueController {
    private final ReissueService reissueService;
    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급 API")
    public SuccessResponse<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return SuccessResponse.ok(reissueService.reissue(request, response));
    }
}
