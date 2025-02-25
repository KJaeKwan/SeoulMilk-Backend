package Seoul_Milk.sm_server.login.controller;

import Seoul_Milk.sm_server.global.dto.response.SuccessResponse;
import Seoul_Milk.sm_server.login.dto.LoginRequestDTO;
import Seoul_Milk.sm_server.login.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    @Operation(summary = "로그인", description = "사번과 비밀번호를 입력해 로그인합니다. 성공 시 cookie에 refreshToken(refresh=~~ 형태로 저장), header에 accessToken을 응답으로 줍니다.",
    responses = {@ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class)),
    headers = {@Header(name = "access", description = "Access Token", schema = @Schema(type = "string")),
            @Header(name = "Set-Cookie", description = "Refresh Token", schema = @Schema(type = "string"))})})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사번", content = @Content(
                    examples = @ExampleObject(
                            name = "USER404",
                            value = "{\n" +
                                    "  \"code\": \"USER404\",\n" +
                                    "  \"message\": 존재하지 않는 사번입니다.\n" +
                                    "  \"result\": \"null\",\n" +
                                    "  \"success\": \"false\n" +
                                    "}"
                    ))),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치", content = @Content(
                    examples = @ExampleObject(
                            name = "USER401",
                            value = "{\n" +
                                    "  \"code\": \"USER401\",\n" +
                                    "  \"message\": 비밀번호가 틀렸습니다.\n" +
                                    "  \"result\": \"null\",\n" +
                                    "  \"success\": \"false\n" +
                                    "}"
                    )))
    })
    @PostMapping("/login")
    public SuccessResponse<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return SuccessResponse.ok(LoginResponseDTO.of("이름", "권한"));
    }
}
