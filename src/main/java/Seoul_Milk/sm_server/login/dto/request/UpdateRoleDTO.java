package Seoul_Milk.sm_server.login.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "관리자 권한이 필요없는 임시 권한 변경 요청 DTO")
public record UpdateRoleDTO(@Schema(description = "변경할 유저 ID", example = "1") Long employeeId,
                            @Schema(description = "변경할 Role", example = "ROLE_ADMIN") String role) {

    @Builder
    public UpdateRoleDTO(
            @JsonProperty("employeeId") Long employeeId,
            @JsonProperty("role") String role
    ) {
        this.employeeId = employeeId;
        this.role = role;
    }
}
