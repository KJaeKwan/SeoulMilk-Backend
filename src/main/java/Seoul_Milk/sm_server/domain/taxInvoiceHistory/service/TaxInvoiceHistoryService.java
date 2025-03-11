package Seoul_Milk.sm_server.domain.taxInvoiceHistory.service;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceHistory.dto.TaxInvoiceHistoryResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoiceHistory.dto.TaxInvoiceHistoryResponseDTO.GetModalResponse;
import Seoul_Milk.sm_server.domain.taxInvoiceHistory.dto.TaxInvoiceHistoryRequestDTO.ChangeTaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoiceHistory.dto.TaxInvoiceHistoryRequestDTO.TaxInvoiceRequest;
import Seoul_Milk.sm_server.login.entity.MemberEntity;

public interface TaxInvoiceHistoryService {
    TaxInvoiceHistoryResponseDTO.TaxInvoiceSearchResult searchByProviderOrConsumer(MemberEntity memberEntity, ProcessStatus processStatus, String poc, int page, int size);

    Void deleteValidationTaxInvoice(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest);

    Void tempSave(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest);

    GetModalResponse showModal(Long taxInvoiceId);

    Void changeColunm(MemberEntity memberEntity, ChangeTaxInvoiceRequest changeTaxInvoiceRequest);
}
