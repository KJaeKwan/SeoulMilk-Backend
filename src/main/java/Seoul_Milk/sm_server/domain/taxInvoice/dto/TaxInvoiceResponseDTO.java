package Seoul_Milk.sm_server.domain.taxInvoice.dto;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "세금 계산서 관련 응답 DTO")
public class TaxInvoiceResponseDTO {

    @Schema(description = "세금 계산서 단일 응답 DTO")
    public record GetOne(
        @Schema(description = "세금 계산서 정보 ID") Long id,
        @Schema(description = "승인번호") String issueId,
        @Schema(description = "공급자 등록번호") String ipId,
        @Schema(description = "공급받는자 등록번호") String suId,
        @Schema(description = "공급가액") int taxTotal,
        @Schema(description = "작성 일자") String erDat,
        @Schema(description = "공급자 상호명") String ipBusinessName,
        @Schema(description = "공급받는자 상호명") String suBusinessName
    ) {
        public static GetOne from(TaxInvoice taxInvoice) {
            return new GetOne(
                    taxInvoice.getTaxInvoiceId(),
                    taxInvoice.getErDat(),
                    taxInvoice.getIpId(),
                    taxInvoice.getSuId(),
                    taxInvoice.getTaxTotal(),
                    taxInvoice.getErDat(),
                    taxInvoice.getIpBusinessName(),
                    taxInvoice.getSuBusinessName()
            );
        }
    }

    @Schema(description = "세금 계산서 리스트 응답 DTO")
    public record GetALL(
            @Schema(description = "세금 계산서 리스트") List<GetOne> taxInvoices
    ) {
        public static GetALL from(List<TaxInvoice> taxInvoiceList) {
            return new GetALL(taxInvoiceList.stream().map(GetOne::from).toList());
        }
    }
}
