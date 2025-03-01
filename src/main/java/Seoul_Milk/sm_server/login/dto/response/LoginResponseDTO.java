package Seoul_Milk.sm_server.login.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponseDTO {
    @JsonProperty("name")
    @Schema(description = "사원이름", example = "김우유")
    private String name;
    @JsonProperty("role")
    @Schema(description = "사원권한(일반 : ROLE_NORMAL, 관리자 : ROLE_ADMIN", example = "ROLE_NORMAL")
    private String role;

    public static LoginResponseDTO of(String name, String role){
        return LoginResponseDTO.builder()
                .name(name)
                .role(role)
                .build();
    }
}
