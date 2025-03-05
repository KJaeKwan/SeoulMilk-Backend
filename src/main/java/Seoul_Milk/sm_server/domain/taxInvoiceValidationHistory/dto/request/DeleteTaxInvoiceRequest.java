package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

@Getter
public class DeleteTaxInvoiceRequest {
    @Schema(description = "검증된 세금계산서 pk값 리스트")
    private List<Long> taxInvoiceIdList;
}
