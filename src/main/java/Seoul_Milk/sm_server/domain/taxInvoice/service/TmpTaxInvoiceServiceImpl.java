package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.entity.TaxInvoice;
import Seoul_Milk.sm_server.domain.taxInvoice.repository.TaxInvoiceRepository;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmpTaxInvoiceServiceImpl implements TmpTaxInvoiceService {

    private final TaxInvoiceRepository taxInvoiceRepository;

    /**
     * 임시 저장된 세금 계산서 전체 조회
     * isTemporary가 true인 모든 데이터를 반환
     */
    @Override
    public List<TaxInvoiceResponseDTO.GetOne> getAll(MemberEntity member) {
        List<TaxInvoice> tempInvoices = taxInvoiceRepository.findTempInvoicesByMember(member);
        return tempInvoices.stream()
                .map(TaxInvoiceResponseDTO.GetOne::from)
                .collect(Collectors.toList());
    }

    /**
     * 리스트로 임시 저장 등록
     */
    @Override
    @Transactional
    public void markAsTemporary(List<Long> taxInvoiceIds, MemberEntity member) {
        List<TaxInvoice> taxInvoices = taxInvoiceRepository.findTempInvoicesByIds(taxInvoiceIds, member);
        for (TaxInvoice taxInvoice : taxInvoices) {
            System.out.println("taxInvoice.getTaxInvoiceId() = " + taxInvoice.getTaxInvoiceId());
        }
        taxInvoices.forEach(taxInvoice -> taxInvoice.updateIsTemp(true));

        taxInvoiceRepository.saveAll(taxInvoices);
    }

    /**
     * 리스트로 임시 저장 해제
     */
    @Override
    @Transactional
    public void removeFromTemporary(List<Long> taxInvoiceIds, MemberEntity member) {
        List<TaxInvoice> taxInvoices = taxInvoiceRepository.findTempInvoicesByIds(taxInvoiceIds, member);
        taxInvoices.forEach(taxInvoice -> taxInvoice.updateIsTemp(false));

        taxInvoiceRepository.saveAll(taxInvoices);
    }
}
