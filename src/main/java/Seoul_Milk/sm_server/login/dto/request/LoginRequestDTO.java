package Seoul_Milk.sm_server.login.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class LoginRequestDTO {
    @Schema(description = "사번", example = "1111")
    private String employeeId;
    @Schema(description = "비밀번호", example = "1234")
    private String password;
}
