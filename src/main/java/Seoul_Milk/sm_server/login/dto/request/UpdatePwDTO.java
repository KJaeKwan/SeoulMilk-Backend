package Seoul_Milk.sm_server.login.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "비밀번호 변경 요청 DTO")
public record UpdatePwDTO(
        @Schema(description = "현재 비밀번호 입력", example = "1234") String currentPassword,
        @Schema(description = "새 비밀번호 입력", example = "9999") String newPassword1,
        @Schema(description = "새 비밀번호 확인", example = "9999") String newPassword2) {

    @Builder
    public UpdatePwDTO(
            @JsonProperty("currentPassword") String currentPassword,
            @JsonProperty("newPassword1") String newPassword1,
            @JsonProperty("newPassword2") String newPassword2
    ) {
        this.currentPassword = currentPassword;
        this.newPassword1 = newPassword1;
        this.newPassword2 = newPassword2;
    }
}
