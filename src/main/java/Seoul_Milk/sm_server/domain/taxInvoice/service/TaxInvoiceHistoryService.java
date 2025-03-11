package Seoul_Milk.sm_server.domain.taxInvoice.service;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceHistoryResponseDTO;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceHistoryResponseDTO.GetModalResponse;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceHistoryRequestDTO.ChangeTaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoice.dto.TaxInvoiceHistoryRequestDTO.TaxInvoiceRequest;
import Seoul_Milk.sm_server.login.entity.MemberEntity;

public interface TaxInvoiceHistoryService {
    TaxInvoiceHistoryResponseDTO.TaxInvoiceSearchResult searchByProviderOrConsumer(MemberEntity memberEntity, ProcessStatus processStatus, String poc, int page, int size);

    Void deleteValidationTaxInvoice(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest);

    Void tempSave(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest);

    GetModalResponse showModal(Long taxInvoiceId);

    Void changeColunm(MemberEntity memberEntity, ChangeTaxInvoiceRequest changeTaxInvoiceRequest);
}
