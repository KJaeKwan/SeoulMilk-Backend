package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface TaxInvoiceRepository {
    TaxInvoice getById(Long id);
    Optional<TaxInvoice> findById(Long id);
    TaxInvoice save(TaxInvoice taxInvoice);
    void delete(Long id);
    Page<TaxInvoice> findByProvider(String provider, Pageable pageable);
    Page<TaxInvoice> findByConsumer(String consumer, Pageable pageable);
    Page<TaxInvoice> findByProviderAndConsumer(String provider, String consumer, Pageable pageable);
    Page<TaxInvoice> findAll(Pageable pageable);
}
