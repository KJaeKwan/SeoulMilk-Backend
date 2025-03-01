package Seoul_Milk.sm_server.domain.taxInvoice.dto;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Schema(description = "세금 계산서 관련 응답 DTO")
public class TaxInvoiceResponseDTO {

    @Schema(description = "세금 계산서 등록했을 때 응답 DTO")
    public record Create(
            @Schema(description = "파일명") String fileName,
            @Schema(description = "추출된 데이터") Map<String, Object> extractedData,
            @Schema(description = "처리 상태") String processStatus,
            @Schema(description = "승인 실패 이유") List<String> errorDetails,
            @Schema(description = "응답 시간") long processingTime
    ) {
        public static Create from(TaxInvoice taxInvoice, String fileName, Map<String, Object> extractedData, List<String> errorDetails, long processingTime) {
            return new Create(
                    fileName,
                    extractedData,
                    taxInvoice.getProcessStatus().name(),
                    errorDetails,
                    processingTime
            );
        }

        public static Create error(String fileName, String errorMessage) {
            return new Create(
                    fileName,
                    Collections.emptyMap(),
                    "UNAPPROVED",
                    Collections.singletonList(errorMessage),
                    0
            );
        }
    }

    @Schema(description = "세금 계산서 단일 응답 DTO")
    public record GetOne(
        @Schema(description = "세금 계산서 정보 ID") Long id,
        @Schema(description = "담당 직원 사번") String employeeId,
        @Schema(description = "승인 상태") String status,
        @Schema(description = "승인번호") String issueId,
        @Schema(description = "공급자 등록번호") String ipId,
        @Schema(description = "공급받는자 등록번호") String suId,
        @Schema(description = "공급가액") int taxTotal,
        @Schema(description = "작성 일자") String erDat,
        @Schema(description = "공급자 상호명") String ipBusinessName,
        @Schema(description = "공급받는자 상호명") String suBusinessName,
        @Schema(description = "공급자 성명") String ipName,
        @Schema(description = "공급받는자 성명") String suName,
        @Schema(description = "에러 상세 내역") List<String> errorDetails
    ) {
        public static GetOne from(TaxInvoice taxInvoice) {
            return new GetOne(
                    taxInvoice.getTaxInvoiceId(),
                    taxInvoice.getMember().getEmployeeId(),
                    taxInvoice.getProcessStatus().name(),
                    taxInvoice.getIssueId(),
                    taxInvoice.getIpId(),
                    taxInvoice.getSuId(),
                    taxInvoice.getTaxTotal(),
                    taxInvoice.getErDat(),
                    taxInvoice.getIpBusinessName(),
                    taxInvoice.getSuBusinessName(),
                    taxInvoice.getIpName(),
                    taxInvoice.getSuName(),
                    taxInvoice.getErrorDetails() != null ? taxInvoice.getErrorDetails() : new ArrayList<>()
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
