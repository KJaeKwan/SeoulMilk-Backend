package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.service;

import Seoul_Milk.sm_server.domain.taxInvoice.enums.ProcessStatus;
import Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.dto.TaxInvoiceValidationHistoryDTO.GetHistoryData;
import Seoul_Milk.sm_server.login.entity.MemberEntity;
import org.springframework.data.domain.Page;

public interface TaxInvoiceValidationService {

    Page<GetHistoryData> showTaxInvoice(ProcessStatus processStatus, MemberEntity memberEntity, int page, int size);
}
