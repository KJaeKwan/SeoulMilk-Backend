package Seoul_Milk.sm_server.domain.taxInvoice.dto.validation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "간편인증한 상태에서의 진위여부 요청 api request")
@Getter
@Builder
public class VerifiedTaxValidationRequestDTO {
    private String key;
}
