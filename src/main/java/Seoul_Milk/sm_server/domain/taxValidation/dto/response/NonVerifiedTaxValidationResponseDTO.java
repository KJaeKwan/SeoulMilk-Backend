package Seoul_Milk.sm_server.domain.taxValidation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Schema(description = "간편인증 안한 상태에서의 진위여부 요청 api response")
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NonVerifiedTaxValidationResponseDTO {
    @Schema(description = "간편인증 후 api에서 사용할 key값")
    private String key;
}
