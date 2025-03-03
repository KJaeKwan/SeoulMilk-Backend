package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxInvoiceJpaRepository extends JpaRepository<TaxInvoice, Long> {
    TaxInvoice findByIssueId(String issueId);
    List<TaxInvoice> findAll();
}
