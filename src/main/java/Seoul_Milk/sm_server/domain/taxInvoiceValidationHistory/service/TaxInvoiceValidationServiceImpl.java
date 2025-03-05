package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service;

import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.APPROVED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.REJECTED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus.UNAPPROVED;
import static Seoul_Milk.sm_server.domain.taxInvoice.enums.TempStatus.INITIAL;
import static Seoul_Milk.sm_server.global.exception.ErrorCode.DO_NOT_ACCESS_OTHER_TAX_INVOICE;
import static Seoul_Milk.sm_server.global.exception.ErrorCode.TAX_INVOICE_NOT_EXIST;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceSearchResult;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.request.DeleteTaxInvoiceRequest;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import jakarta.transaction.Transactional;
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

        List<Long> taxInvoiceIds = taxInvoicePage.getContent().stream()
                .map(TaxInvoice::getTaxInvoiceId)
                .toList();
        //임시저장 상태가 INITIAL인건 모두 Untemp로 바꾸기
        taxInvoiceRepository.updateInitialToUntemp(taxInvoiceIds);

        List<GetHistoryData> historyDataList = taxInvoicePage.stream()
                .map(taxInvoice -> TaxInvoiceValidationHistoryDTO.GetHistoryData.from(taxInvoice, taxInvoice.getFile()))
                .toList();

        Page<GetHistoryData> pageGetHistoryData = new PageImpl<>(historyDataList, pageable, taxInvoicePage.getTotalElements());
        return TaxInvoiceSearchResult.GetData.from(pageGetHistoryData, total, approved, rejected, unapproved);
    }

    @Transactional
    @Override
    public Void deleteValidationTaxInvoice(MemberEntity memberEntity, DeleteTaxInvoiceRequest deleteTaxInvoiceRequest) {
        List<Long> taxInvoiceIdList = deleteTaxInvoiceRequest.getTaxInvoiceIdList();
        List<TaxInvoice> taxInvoices = taxInvoiceRepository.findAllById(taxInvoiceIdList);

        // 존재하지 않는 ID가 있다면 예외 발생
        if (taxInvoices.size() != taxInvoiceIdList.size()) {
            throw new CustomException(TAX_INVOICE_NOT_EXIST);
        }

        // 다른 사용자의 세금계산서를 삭제하려 하면 예외 발생
        boolean hasUnauthorizedAccess = taxInvoices.stream()
                .anyMatch(invoice -> !invoice.getMember().getId().equals(memberEntity.getId()));

        if (hasUnauthorizedAccess) {
            throw new CustomException(DO_NOT_ACCESS_OTHER_TAX_INVOICE);
        }

        // 한 번의 deleteAll() 호출로 일괄 삭제
        taxInvoiceRepository.deleteAll(taxInvoices);
        return null;
    }
}
