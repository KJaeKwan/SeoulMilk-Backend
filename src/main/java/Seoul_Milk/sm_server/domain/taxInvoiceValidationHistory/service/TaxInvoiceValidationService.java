package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceSearchResult;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetModalResponse;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryRequestDTO.ChangeTaxInvoiceRequest;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryRequestDTO.TaxInvoiceRequest;
import Seoul_Milk.sm_server.login.entity.MemberEntity;

public interface TaxInvoiceValidationService {
    TaxInvoiceSearchResult.GetData searchByProviderOrConsumer(MemberEntity memberEntity, ProcessStatus processStatus, String poc, int page, int size);

    Void deleteValidationTaxInvoice(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest);

    Void tempSave(MemberEntity memberEntity, TaxInvoiceRequest taxInvoiceRequest);

    GetModalResponse showModal(Long taxInvoiceId);

    Void changeColunm(MemberEntity memberEntity, ChangeTaxInvoiceRequest changeTaxInvoiceRequest);
}
