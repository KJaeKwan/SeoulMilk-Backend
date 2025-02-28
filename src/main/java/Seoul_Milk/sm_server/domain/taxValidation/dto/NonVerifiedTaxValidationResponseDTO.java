package Seoul_Milk.sm_server.domain.taxValidation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "간편인증 안한 상태에서의 진위여부 요청 api response body")
public class NonVerifiedTaxValidationResponseDTO {
    @Schema(description = "잡 인덱스")
    private int jobIndex;
    @Schema(description = "스레드 인덱스")
    private int threadIndex;
    @Schema(description = "트렌젝션 아이디")
    private String jti;
    @Schema(description = "추가 인증 시간")
    private Long twoWayTimestamp;
}
