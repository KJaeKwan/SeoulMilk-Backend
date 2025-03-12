package Seoul_Milk.sm_server.global.auth.controller;

import Seoul_Milk.sm_server.global.response.SuccessResponse;
import Seoul_Milk.sm_server.global.auth.service.ReissueService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReissueController {
    private final ReissueService reissueService;
    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급API", description = "refreshToken을 쿠키(refresh=~~ 형태로 요청)에 담아 요청하면 refresh/access Token 재발급\n 응답쿠키로 리프레쉬 토큰, 헤더로 액세스 토큰 전달")
    public SuccessResponse<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return SuccessResponse.ok(reissueService.reissue(request, response));
    }
}
