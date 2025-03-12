package Seoul_Milk.sm_server.global.infrastructure.mail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "이메일 인증 확인 요청 DTO")
public record VerifyEmailRequestDTO(
        @Schema(description = "이메일", example = "user@seoulmilk.co.kr") String email,
        @Schema(description = "인증번호", example = "XXXXXX") String authCode
) {
    @Builder
    public VerifyEmailRequestDTO(
            @JsonProperty("email") String email,
            @JsonProperty("authCode") String authCode) {
        this.email = email;
        this.authCode = authCode;
    }
}
