package Seoul_Milk.sm_server.domain.taxInvoice.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TaxInvoiceExcelColumn {
    INDEX("순번"),
    SU_NAME("공급자명"),
    IP_NAME("공급받는자명"),
    ISSUE_ID("승인번호"),
    ARAP("매출매입구분"),
    ER_DAT("전자세금계산서 작성일자"),
    SU_ID("공급자 사업자등록번호"),
    IP_ID("공급받는자 사업자등록번호"),
    CHARGE_TOTAL("총공급가액 합계"),
    TAX_TOTAL("총 세액 합계"),
    GRAND_TOTAL("총액(공급가액 + 세액)"),
    CREATION_DATE("생성일"),
    CREATION_TIME("생성시간");

    private final String headerName;

    TaxInvoiceExcelColumn(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }

    public static List<String> getHeaders() {
        return Arrays.stream(TaxInvoiceExcelColumn.values())
                .map(TaxInvoiceExcelColumn::getHeaderName)
                .toList();
    }

    public static Map<TaxInvoiceExcelColumn, Integer> getHeaderIndices() {
        Map<TaxInvoiceExcelColumn, Integer> indices = new HashMap<>();
        TaxInvoiceExcelColumn[] values = TaxInvoiceExcelColumn.values();
        for (int i = 0; i < values.length; i++) {
            indices.put(values[i], i);
        }
        return indices;
    }
}
