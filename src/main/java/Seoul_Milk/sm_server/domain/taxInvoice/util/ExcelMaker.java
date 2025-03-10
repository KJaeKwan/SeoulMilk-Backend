package Seoul_Milk.sm_server.domain.taxInvoice.util;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.ARAP;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.CHARGE_TOTAL;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.CREATION_DATE;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.CREATION_TIME;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.ER_DAT;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.GRAND_TOTAL;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.INDEX;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.IP_ID;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.IP_NAME;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.ISSUE_ID;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.SU_ID;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.SU_NAME;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn.TAX_TOTAL;
import static org.apache.poi.ss.util.CellUtil.createCell;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.TaxInvoiceExcelColumn;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class ExcelMaker {
    public ByteArrayInputStream getTaxInvoiceToExcel(List<TaxInvoice> taxInvoiceList) throws IOException{
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 엑셀 파일 작업 수행
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLACK.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 4. Create Sheets
            Sheet taxInvoiceSheet = workbook.createSheet("TaxInvoice");

            // 5. Write Dashboard Sheet
            createDashboardSheet(taxInvoiceList, taxInvoiceSheet, headerCellStyle);

            // 6. Write File
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public void createDashboardSheet(List<TaxInvoice> taxInvoiceList, Sheet sheet, CellStyle headerCellStyle) {
        // 1. Create header row
        Row headerRow = sheet.createRow(0);
        List<String> headerStrings = TaxInvoiceExcelColumn.getHeaders();
        Map<TaxInvoiceExcelColumn, Integer> headerIndices = TaxInvoiceExcelColumn.getHeaderIndices();

        IntStream.range(0, headerStrings.size())
                .forEach(i -> createCell(headerRow, i, headerStrings.get(i), headerCellStyle));

        // 2. Create rows
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int rowIndex = 1;
        Row bodyRow = null;
        for(TaxInvoice taxInvoice : taxInvoiceList) {
            bodyRow = sheet.createRow(rowIndex++);

            createCell(bodyRow, headerIndices.get(INDEX), String.valueOf(rowIndex - 1), null);
            createCell(bodyRow, headerIndices.get(SU_NAME), taxInvoice.getSuName(), null);
            createCell(bodyRow, headerIndices.get(IP_NAME), taxInvoice.getIpName(), null);
            createCell(bodyRow, headerIndices.get(ISSUE_ID), taxInvoice.getIssueId(), null);
            createCell(bodyRow, headerIndices.get(ARAP), taxInvoice.getArap().toString(), null);
            createCell(bodyRow, headerIndices.get(ER_DAT), taxInvoice.getErDat(), null);
            createCell(bodyRow, headerIndices.get(SU_ID), taxInvoice.getSuId(), null);
            createCell(bodyRow, headerIndices.get(IP_ID), taxInvoice.getIpId(), null);
            createCell(bodyRow, headerIndices.get(CHARGE_TOTAL), String.valueOf(taxInvoice.getChargeTotal()), null);
            createCell(bodyRow, headerIndices.get(TAX_TOTAL), String.valueOf(taxInvoice.getTaxTotal()), null);
            createCell(bodyRow, headerIndices.get(GRAND_TOTAL), String.valueOf(taxInvoice.getGrandTotal()), null);

            LocalDateTime createAt = taxInvoice.getCreateAt();
            createCell(bodyRow, headerIndices.get(CREATION_DATE), createAt.format(dayFormatter), null);
            createCell(bodyRow, headerIndices.get(CREATION_TIME), createAt.format(timeFormatter), null);
        }

        // 3. Set Column Width
        IntStream.range(0, headerStrings.size())
                .forEach(i -> {
                    sheet.autoSizeColumn(i);
                    sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
                });
    }
}
