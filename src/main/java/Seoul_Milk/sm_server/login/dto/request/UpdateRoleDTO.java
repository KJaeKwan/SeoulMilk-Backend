package Seoul_Milk.sm_server.login.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "관리자 권한이 필요없는 임시 권한 변경 요청 DTO")
public record UpdateRoleDTO(@Schema(description = "변경할 유저 ID", example = "1") Long memberId,
                            @Schema(description = "변경할 Role", example = "ROLE_ADMIN") String role) {

    @Builder
    public UpdateRoleDTO(
            @JsonProperty("memberId") Long memberId,
            @JsonProperty("role") String role
    ) {
        this.memberId = memberId;
        this.role = role;
    }
}
