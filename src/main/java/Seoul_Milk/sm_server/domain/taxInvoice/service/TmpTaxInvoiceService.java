package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceResponseDTO;
import Seoul_Milk.sm_server.login.entity.MemberEntity;

import java.util.List;

public interface TmpTaxInvoiceService {
    List<TaxInvoiceResponseDTO.GetOne> getAll(MemberEntity member);
    void markAsTemporary(List<Long> taxInvoiceIds, MemberEntity member);
    void removeFromTemporary(List<Long> taxInvoiceIds, MemberEntity member);
}
