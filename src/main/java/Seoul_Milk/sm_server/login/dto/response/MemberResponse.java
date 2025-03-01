package Seoul_Milk.sm_server.login.dto.response;

import Seoul_Milk.sm_server.login.entity.MemberEntity;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "사용자 정보 응답 DTO")
public record MemberResponse(
        @Schema(description = "회원 ID") Long id,
        @Schema(description = "이름") String name,
        @Schema(description = "이메일") String email,
        @Schema(description = "사번") String employeeId,
        @Schema(description = "권한") String role
) {
    public static MemberResponse from(MemberEntity memberEntity) {
        return new MemberResponse(
                memberEntity.getId(),
                memberEntity.getName(),
                memberEntity.getEmail(),
                memberEntity.getEmployeeId(),
                memberEntity.getRole().name()
        );
    }
}
