package Seoul_Milk.sm_server.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사원 등록 요청 DTO")
public record RegisterDTO(
        @Schema(description = "이름", example = "사원1") String name,
        @Schema(description = "사번", example = "1111") String employeeId,
        @Schema(description = "권한", example = "ROLE_NORMAL") String role
) {}
