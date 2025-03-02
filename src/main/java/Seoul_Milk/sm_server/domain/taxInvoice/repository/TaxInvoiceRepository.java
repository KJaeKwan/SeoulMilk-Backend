package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;

import java.util.List;
import java.util.Optional;

public interface TaxInvoiceRepository {
    TaxInvoice getById(Long id);
    Optional<TaxInvoice> findById(Long id);
    List<TaxInvoice> findAll();
    TaxInvoice save(TaxInvoice taxInvoice);
    void delete(Long id);
    TaxInvoice findByIssueId(String issueId);
}
