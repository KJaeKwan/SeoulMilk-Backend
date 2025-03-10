package Seoul_Milk.sm_server.domain.taxInvoiceValidation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "세금 계산서 진위여부 판별을 위한 필수컬럼")
@Getter
public class TaxInvoiceInfo {
    @Schema(description = "공급자 등록번호")
    private String supplierRegNumber;
    @Schema(description = "공급받는자 등록번호")
    private String contractorRegNumber;
    @Schema(description = "승인번호")
    private String approvalNo;
    @Schema(description = "작성일자")
    private String reportingDate;
    @Schema(description = "공급가액")
    private String supplyValue;
}
