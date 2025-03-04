package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto;

import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetHistoryData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;


@AllArgsConstructor
public class TaxInvoiceSearchResult {
    public record GetData(
            @Schema(description = "검증된 세금계산서 페이징 결과") Page<GetHistoryData> page,
            @Schema(description = "전체 데이터 수") Long total,
            @Schema(description = "승인 데이터 수") Long approved,
            @Schema(description = "반려 데이터 수") Long rejected,
            @Schema(description = "검증실패 데이터 수") Long unapproved
    ){
        public static GetData from(
                Page<GetHistoryData> page,
                Long total,
                Long approved,
                Long rejected,
                Long unapproved
        ){
            return new GetData(
                    page,
                    total,
                    approved,
                    rejected,
                    unapproved
            );
        }
    }
}
