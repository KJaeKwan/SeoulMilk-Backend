package Seoul_Milk.sm_server.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class JoinDTO {
    @Schema(description = "사번", example = "1234")
    private String employeeId;

    @Schema(description = "비밀번호", example = "password00.")
    private String password;
}
