package Seoul_Milk.sm_server.domain.taxValidation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "간편인증한 상태에서의 진위여부 요청 api request")
@Getter
public class VerifiedTaxValidationRequestDTO {
    private String key;
}
