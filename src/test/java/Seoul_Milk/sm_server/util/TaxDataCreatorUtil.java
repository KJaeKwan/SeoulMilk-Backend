package Seoul_Milk.sm_server.util;

import Seoul_Milk.sm_server.domain.member.entity.MemberEntity;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ArapType;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import java.time.LocalDateTime;

public class TaxDataCreatorUtil {
    public static TaxInvoice createTaxInvoice(Long id, String issueId, ProcessStatus status, String ipId, String suId, String erDat, String ipBusinessName, String suBusinessName,
            MemberEntity member) {
        return TaxInvoice.builder()
                .taxInvoiceId(id)
                .issueId(issueId)
                .arap(ArapType.SALES)
                .processStatus(status)
                .ipId(ipId)
                .suId(suId)
                .chargeTotal(1)
                .erDat(erDat)
                .ipBusinessName(ipBusinessName)
                .suBusinessName(suBusinessName)
                .member(member)
                .createAt(LocalDateTime.now())
                .build();
    }

    public static TaxInvoiceFile createTaxInvoiceFile(Long id, TaxInvoice invoice) {
        TaxInvoiceFile file = TaxInvoiceFile.builder()
                .id(id)
                .taxInvoice(invoice)
                .build();
        invoice.attachFile(file);
        return file;
    }
}
