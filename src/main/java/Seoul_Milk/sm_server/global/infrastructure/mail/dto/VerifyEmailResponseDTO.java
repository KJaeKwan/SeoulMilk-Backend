package Seoul_Milk.sm_server.global.infrastructure.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyEmailResponseDTO {

    @Schema(description = "성공 여부", example = "true")
    private boolean success;

    @Schema(description = "메세지", example = "이메일 인증에 성공했습니다.")
    private String message;

    public static VerifyEmailResponseDTO of(boolean success) {
        return VerifyEmailResponseDTO.builder()
                .success(success)
                .message(success ? "이메일 인증에 성공했습니다." : "이메일 인증에 실패했습니다.")
                .build();
    }
}
