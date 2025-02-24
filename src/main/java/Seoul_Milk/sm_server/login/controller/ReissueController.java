package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.LoginResponseDTO;
import Seoul_Milk.sm_server.login.service.ReissueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "토큰 재발급API", description = "refreshToken을 쿠키에 담아 요청하면 refresh/access Token 재발급",
            parameters = {
                    @Parameter(
                            in = ParameterIn.COOKIE,
                            name = "refresh", // 쿠키의 이름
                            description = "refreshToken",
                            required = true, // 필수 여부
                            schema = @Schema(type = "string")
                    )
            },
            responses = {@ApiResponse(responseCode = "200", description = "재발급 성공",
                    headers = {@Header(name = "access", description = "Access Token", schema = @Schema(type = "string")),
                            @Header(name = "Set-Cookie", description = "Refresh Token", schema = @Schema(type = "string"))})})

    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "존재하지 않는 리플레쉬 토큰", content = @Content(
                    examples = @ExampleObject(
                            name = "TOKEN404",
                            value = "{\n" +
                                    "  \"code\": \"TOKEN404\",\n" +
                                    "  \"message\": 해당 사용자에 대한 Refresh Token 을 찾을 수 없습니다.\n" +
                                    "  \"result\": \"null\",\n" +
                                    "  \"success\": \"false\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "401", description = "리프레쉬 토큰 만료", content = @Content(
                    examples = @ExampleObject(
                            name = "TOKEN401",
                            value = "{\n" +
                                    "  \"code\": \"TOKEN401\",\n" +
                                    "  \"message\": Refresh Token 이 만료되었습니다.\n" +
                                    "  \"result\": \"null\",\n" +
                                    "  \"success\": \"false\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 리프레쉬 토큰", content = @Content(
                    examples = @ExampleObject(
                            name = "TOKEN401",
                            value = "{\n" +
                                    "  \"code\": \"TOKEN401\",\n" +
                                    "  \"message\": 유효하지 않은 Refresh Token 입니다.\n" +
                                    "  \"result\": \"null\",\n" +
                                    "  \"success\": \"false\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰", content = @Content(
                    examples = @ExampleObject(
                            name = "TOKEN401",
                            value = "{\n" +
                                    "  \"code\": \"TOKEN401\",\n" +
                                    "  \"message\": 유효하지 않은 Token 입니다.\n" +
                                    "  \"result\": \"null\",\n" +
                                    "  \"success\": \"false\n" +
                                    "}"
                    )))
    })
    public SuccessResponse<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return SuccessResponse.ok(reissueService.reissue(request, response));
    }
}
