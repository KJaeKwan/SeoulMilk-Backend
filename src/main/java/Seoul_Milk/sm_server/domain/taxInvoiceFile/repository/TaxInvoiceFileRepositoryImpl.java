package Seoul_Milk.sm_server.domain.taxInvoiceFile.repository;

import Seoul_Milk.sm_server.domain.taxInvoiceFile.entity.TaxInvoiceFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaxInvoiceFileRepositoryImpl implements TaxInvoiceFileRepository {

    private final TaxInvoiceFileJpaRepository taxInvoiceFileJpaRepository;

    @Override
    public TaxInvoiceFile save(TaxInvoiceFile taxInvoiceFile) {
        return taxInvoiceFileJpaRepository.save(taxInvoiceFile);
    }
}
