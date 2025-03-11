package Seoul_Milk.sm_server.domain.taxInvoiceHistory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "RE 관련 DTO")
public class TaxInvoiceHistoryRequestDTO {
    public record ChangeTaxInvoiceRequest(
            @Schema(description = "수정하고 싶은 taxInvoiceId") Long taxInvoiceId,
            @Schema(description = "수정된 승인번호") String issueId,
            @Schema(description = "수정된 작성일자") String erDat,
            @Schema(description = "수정된 공급자 사업자 등록번호") String suId,
            @Schema(description = "수정된 공급받는자 사업자 등록번호") String ipId,
            @Schema(description = "수정된 공급가액") int chargeTotal

    ){
    }
    public record TaxInvoiceRequest(
            @Schema(description = "검증된 세금계산서 pk값 리스트") List<Long> taxInvoiceIdList
    ){
    }
}
