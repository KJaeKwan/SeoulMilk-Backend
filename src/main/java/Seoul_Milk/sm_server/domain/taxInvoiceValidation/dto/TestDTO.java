package Seoul_Milk.sm_server.domain.taxInvoiceValidation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TestDTO {
    private String supplierRegNumber;
    private String contractorRegNumber;
    private String approvalNo;
    private String reportingDate;
    private String supplyValue;
}
