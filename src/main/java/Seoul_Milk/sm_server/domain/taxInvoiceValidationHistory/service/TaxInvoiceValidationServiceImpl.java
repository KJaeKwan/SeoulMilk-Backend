package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.validator.PocValidator;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaxInvoiceValidationServiceImpl implements TaxInvoiceValidationService{
    private final TaxInvoiceRepository taxInvoiceRepository;
    private final PocValidator pocValidator;

    @Override
    public Page<GetHistoryData> showTaxInvoice(ProcessStatus processStatus, MemberEntity memberEntity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaxInvoice> taxInvoicePage = taxInvoiceRepository.searchWithFilters(
                null, null, null, memberEntity, null, null, processStatus, pageable
        );

        List<GetHistoryData> historyDataList = taxInvoicePage.stream()
                .map(taxInvoice -> TaxInvoiceValidationHistoryDTO.GetHistoryData.from(taxInvoice, taxInvoice.getFile()))
                .toList();

        return new PageImpl<>(historyDataList, pageable, taxInvoicePage.getTotalElements());
    }

    @Override
    public Page<GetHistoryData> searchByProviderOrConsumer(MemberEntity memberEntity, String poc, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        pocValidator.validate(poc); // 파라미터 검증
        Page<TaxInvoice> taxInvoicePage = taxInvoiceRepository.searchConsumerOrProvider(poc, memberEntity.getEmployeeId(), memberEntity, pageable);
        List<GetHistoryData> historyDataList = taxInvoicePage.stream()
                .map(taxInvoice -> TaxInvoiceValidationHistoryDTO.GetHistoryData.from(taxInvoice, taxInvoice.getFile()))
                .toList();
        return new PageImpl<>(historyDataList, pageable, taxInvoicePage.getTotalElements());
    }
}
