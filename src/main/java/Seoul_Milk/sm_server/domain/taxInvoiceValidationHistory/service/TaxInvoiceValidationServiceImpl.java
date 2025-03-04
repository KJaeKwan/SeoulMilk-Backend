package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.APPROVED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.REJECTED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.UNAPPROVED;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceSearchResult;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetHistoryData;
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

    @Override
    public TaxInvoiceSearchResult.GetData searchByProviderOrConsumer(MemberEntity memberEntity, ProcessStatus processStatus, String poc, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaxInvoice> taxInvoicePage = taxInvoiceRepository.searchConsumerOrProvider(poc, memberEntity.getEmployeeId(), processStatus, memberEntity, pageable);
        Long total = taxInvoiceRepository.getProcessStatusCount(null, memberEntity);
        Long approved = taxInvoiceRepository.getProcessStatusCount(APPROVED, memberEntity);
        Long rejected = taxInvoiceRepository.getProcessStatusCount(REJECTED, memberEntity);
        Long unapproved = taxInvoiceRepository.getProcessStatusCount(UNAPPROVED, memberEntity);

        List<GetHistoryData> historyDataList = taxInvoicePage.stream()
                .map(taxInvoice -> TaxInvoiceValidationHistoryDTO.GetHistoryData.from(taxInvoice, taxInvoice.getFile()))
                .toList();

        Page<GetHistoryData> pageGetHistoryData = new PageImpl<>(historyDataList, pageable, taxInvoicePage.getTotalElements());
        return TaxInvoiceSearchResult.GetData.from(pageGetHistoryData, total, approved, rejected, unapproved);
    }
}
