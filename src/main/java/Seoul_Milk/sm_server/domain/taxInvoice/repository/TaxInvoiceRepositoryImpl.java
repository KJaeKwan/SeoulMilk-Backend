package Seoul_Milk.sm_server.domain.taxInvoice.repository;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaxInvoiceRepositoryImpl implements TaxInvoiceRepository {

    private final TaxInvoiceJpaRepository taxInvoiceJpaRepository;

    @Override
    public TaxInvoice getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TAX_INVOICE_NOT_EXIST));
    }

    @Override
    public Optional<TaxInvoice> findById(Long id) {
        return taxInvoiceJpaRepository.findById(id);
    }

    @Override
    public List<TaxInvoice> findAll() {
        return taxInvoiceJpaRepository.findAll();
    }

    @Override
    public TaxInvoice save(TaxInvoice taxInvoice) {
        return taxInvoiceJpaRepository.save(taxInvoice);
    }

    @Override
    public void delete(Long id) {
        taxInvoiceJpaRepository.deleteById(id);
    }

    @Override
    public TaxInvoice findByIssueId(String issueId) {
        return taxInvoiceJpaRepository.findByIssueId(issueId);
    }
}
