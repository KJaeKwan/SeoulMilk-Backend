package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "RE_01관련 DTO")
public class TaxInvoiceValidationHistoryDTO {

    @Schema(description = "검증 내역 조회 결과 단일 DTO")
    public record GetHistoryData(
            @Schema(description = "공급자명") String suBusinessName,
            @Schema(description = "공급받는자명") String ipBusinessName,
            @Schema(description = "생성날짜") LocalDateTime createdAt,
            @Schema(description = "파일url") String url,
            @Schema(description = "승인여부") ProcessStatus processStatus
    ){
        public static GetHistoryData from(
                TaxInvoice taxInvoice, TaxInvoiceFile taxInvoiceFile) {
            return new GetHistoryData(
                taxInvoice.getSuBusinessName(),
                taxInvoice.getIpBusinessName(),
                taxInvoice.getCreatedAt(),
                taxInvoiceFile.getFileUrl(),
                taxInvoice.getProcessStatus()
            );
        }
    }


}
