package Seoul_Milk.sm_server.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "비밀번호 확인 요청 DTO")
public record VerifyPwDTO(
        @Schema(description = "비밀번호 입력", example = "1234") String password
) {

    @Builder
    public VerifyPwDTO(
            @JsonProperty("password") String password
    ) {
        this.password = password;
    }
}
