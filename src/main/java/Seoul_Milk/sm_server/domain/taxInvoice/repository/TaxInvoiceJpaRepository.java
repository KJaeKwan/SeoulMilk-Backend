package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxInvoiceJpaRepository extends JpaRepository<TaxInvoice, Long> {
}
