package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;

@Schema(description = "RE 관련 DTO")
public class TaxInvoiceValidationHistoryResponseDTO {

    @Schema(description = "검증 내역 조회 결과 단일 DTO")
    public record GetHistoryData(
            @Schema(description = "검증된 세금계산서 pk값") Long id,
            @Schema(description = "공급자명") String suBusinessName,
            @Schema(description = "공급받는자명") String ipBusinessName,
            @Schema(description = "생성날짜") LocalDateTime createdAt,
            @Schema(description = "파일url") String url,
            @Schema(description = "승인여부") ProcessStatus processStatus
    ){
        public static GetHistoryData from(
                TaxInvoice taxInvoice, TaxInvoiceFile taxInvoiceFile) {
            return new GetHistoryData(
                taxInvoice.getTaxInvoiceId(),
                taxInvoice.getIpBusinessName(),
                taxInvoice.getSuBusinessName(),
                taxInvoice.getCreateAt(),
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
            @Schema(description = "세액") String taxTotal,
            @Schema(description = "공급가액") String chargeTotal,
            @Schema(description = "총액") String grandTotal,
            @Schema(description = "처리현황") ProcessStatus processStatus,
            @Schema(description = "파일url") String url
    ){
        public static GetModalResponse from(
                TaxInvoice taxInvoice
        ){
            return new GetModalResponse(
                    taxInvoice.getIssueId(),
                    taxInvoice.getErDat(),
                    taxInvoice.getIpBusinessName(),
                    taxInvoice.getSuId(),
                    taxInvoice.getSuBusinessName(),
                    taxInvoice.getIpId(),
                    String.valueOf(taxInvoice.getTaxTotal()),
                    String.valueOf(taxInvoice.getChargeTotal()),
                    String.valueOf(taxInvoice.getGrandTotal()),
                    taxInvoice.getProcessStatus(),
                    taxInvoice.getFile().getFileUrl()
            );
        }
    }

    @Schema(description = "검색 결과 통계 DTO")
    public record TaxInvoiceSearchResult (
            @Schema(description = "검증된 세금계산서 페이징 결과") Page<GetHistoryData> page,
            @Schema(description = "전체 데이터 수") Long total,
            @Schema(description = "승인 데이터 수") Long approved,
            @Schema(description = "반려 데이터 수") Long rejected,
            @Schema(description = "검증실패 데이터 수") Long unapproved
    ) {
        public static TaxInvoiceSearchResult from(
                Page<GetHistoryData> page,
                Long total,
                Long approved,
                Long rejected,
                Long unapproved
        ){
            return new TaxInvoiceSearchResult(
                    page,
                    total,
                    approved,
                    rejected,
                    unapproved
            );
        }
    }

}
