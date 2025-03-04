package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service;

import static Seoul_Milk.sm_server.global.exception.ErrorCode.DO_NOT_ACCESS_OTHER_TAX_INVOICE;
import static Seoul_Milk.sm_server.global.exception.ErrorCode.TAX_INVOICE_NOT_EXIST;

import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetHistoryData;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.request.DeleteTaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.validator.PocValidator;
import Seoul_Milk.sm_server.global.exception.CustomException;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    @Override
    public Void deleteValidationTaxInvoice(MemberEntity memberEntity, DeleteTaxInvoiceRequest deleteTaxInvoiceRequest) {
        List<Long> taxInvoiceIdList = deleteTaxInvoiceRequest.getTaxInvoiceIdList();
        System.out.println(taxInvoiceIdList);
        List<TaxInvoice> taxInvoices = taxInvoiceRepository.findAllById(taxInvoiceIdList);
        System.out.println(taxInvoices.size());

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
