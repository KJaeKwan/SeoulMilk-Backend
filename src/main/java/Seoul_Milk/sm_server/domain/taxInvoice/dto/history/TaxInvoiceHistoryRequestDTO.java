package Seoul_Milk.sm_server.domain.taxInvoice.dto.history;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

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
        @Builder
        public ChangeTaxInvoiceRequest(
                Long taxInvoiceId,
                String issueId,
                String erDat,
                String suId,
                String ipId,
                int chargeTotal
        ){
            this.taxInvoiceId = taxInvoiceId;
            this.issueId = issueId;
            this.erDat = erDat;
            this.suId = suId;
            this.ipId = ipId;
            this.chargeTotal = chargeTotal;
        }
    }
    public record TaxInvoiceRequest(
            @Schema(description = "검증된 세금계산서 pk값 리스트") List<Long> taxInvoiceIdList
    ){
        @Builder
        public TaxInvoiceRequest(List<Long> taxInvoiceIdList){
            this.taxInvoiceIdList = taxInvoiceIdList;
        }
    }
}
