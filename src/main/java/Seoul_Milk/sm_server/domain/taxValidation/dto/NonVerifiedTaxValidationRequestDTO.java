package Seoul_Milk.sm_server.domain.taxValidation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;

//Todo controller이름 정해지면 description 그거에 맞추기
@Getter
public class NonVerifiedTaxValidationRequestDTO {
    @Schema(description = "로그인 타입, ")
    private String loginTypeLevel;
    private String userName;
    private String phoneNo;
    private String identity;
    private String telecom;
    private List<TaxInvoiceInfo> validRequestList;
}
