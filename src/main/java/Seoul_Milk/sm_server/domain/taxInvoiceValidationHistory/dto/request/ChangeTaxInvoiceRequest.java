package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ChangeTaxInvoiceRequest {
    @Schema(description = "수정하고 싶은 taxInvoiceId")
    private Long taxInvoiceId;
    @Schema(description = "수정된 승인번호")
    private String issueId;
    @Schema(description = "수정된 작성일자")
    private String erDat;
    @Schema(description = "수정된 공급자 사업자 등록번호")
    private String suId;
    @Schema(description = "수정된 공급받는자 사업자 등록번호")
    private String ipId;
    @Schema(description = "수정된 공급가액")
    private int chargeTotal;
}
