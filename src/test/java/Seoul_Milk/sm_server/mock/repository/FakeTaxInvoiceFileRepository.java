package Seoul_Milk.sm_server.mock.repository;

import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import Seoul_Milk.sm_server.domain.taxInvoiceFile.repository.TaxInvoiceFileRepository;

public class FakeTaxInvoiceFileRepository implements TaxInvoiceFileRepository {
    @Override
    public TaxInvoiceFile save(TaxInvoiceFile taxInvoiceFile) {
        return null;
    }
}
