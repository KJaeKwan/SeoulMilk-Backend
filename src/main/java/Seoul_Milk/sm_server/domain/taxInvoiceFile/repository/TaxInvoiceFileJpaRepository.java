package Seoul_Milk.sm_server.domain.taxInvoiceFile.repository;

import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxInvoiceFileJpaRepository extends JpaRepository<TaxInvoiceFile, Long> {
}
