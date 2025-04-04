package Seoul_Milk.sm_server.domain.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "비밀번호 초기화 요청 DTO")
public record ResetPwDTO(
        @Schema(description = "사번 입력", example = "1234") String employeeId,
        @Schema(description = "비밀번호 입력", example = "1234") String password1,
        @Schema(description = "비밀번호 확인", example = "1234") String password2
) {
    @Builder
    public ResetPwDTO(
            @JsonProperty("employeeId") String employeeId,
            @JsonProperty("password1") String password1,
            @JsonProperty("password2") String password2
    ) {
        this.employeeId = employeeId;
        this.password1 = password1;
        this.password2 = password2;
    }
}
