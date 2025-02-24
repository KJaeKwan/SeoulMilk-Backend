package Seoul_Milk.sm_server.login.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponseDTO {
    @JsonProperty("name")
    private String name;
    @JsonProperty("role")
    private String role;

    public static LoginResponseDTO of(String name, String role){
        return LoginResponseDTO.builder()
                .name(name)
                .role(role)
                .build();
    }
}
