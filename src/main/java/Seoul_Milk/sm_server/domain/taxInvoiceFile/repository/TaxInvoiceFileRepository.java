package Seoul_Milk.sm_server.domain.taxInvoiceFile.repository;

import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;

public interface TaxInvoiceFileRepository {
    TaxInvoiceFile save(TaxInvoiceFile taxInvoiceFile);
}
