package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "RE 관련 DTO")
public class TaxInvoiceValidationHistoryDTO {

    @Schema(description = "검증 내역 조회 결과 단일 DTO")
    public record GetHistoryData(
            @Schema(description = "검증된 세금계산서 pk값") Long id,
            @Schema(description = "공급자명") String suName,
            @Schema(description = "공급받는자명") String ipName,
            @Schema(description = "생성날짜") LocalDateTime createdAt,
            @Schema(description = "파일url") String url,
            @Schema(description = "승인여부") ProcessStatus processStatus
    ){
        public static GetHistoryData from(
                TaxInvoice taxInvoice, TaxInvoiceFile taxInvoiceFile) {
            return new GetHistoryData(
                taxInvoice.getTaxInvoiceId(),
                taxInvoice.getSuName(),
                taxInvoice.getIpName(),
                taxInvoice.getErDat(),
                taxInvoiceFile.getFileUrl(),
                taxInvoice.getProcessStatus()
            );
        }
    }

    @Schema(description = "승인/반려/수정됨 결과 조회 모달 응답 DTO")
    public record GetModalResponse(
            @Schema(description = "승인번호") String issueId,
            @Schema(description = "작성일자") String erDat,
            @Schema(description = "공급자명") String suName,
            @Schema(description = "공급자 등록번호") String suId,
            @Schema(description = "공급받는자명") String ipName,
            @Schema(description = "공급받는자 등록번호") String ipId,
            @Schema(description = "공급가액") String taxTotal,
            @Schema(description = "처리현황") ProcessStatus processStatus,
            @Schema(description = "파일url") String url
    ){
        public static GetModalResponse from(
                TaxInvoice taxInvoice
        ){
            return new GetModalResponse(
                    taxInvoice.getIssueId(),
                    taxInvoice.getIssueDate(),
                    taxInvoice.getSuName(),
                    taxInvoice.getSuId(),
                    taxInvoice.getIpName(),
                    taxInvoice.getIpId(),
                    String.valueOf(taxInvoice.getTaxTotal()),
                    taxInvoice.getProcessStatus(),
                    taxInvoice.getFile().getFileUrl()
            );
        }
    }

}
