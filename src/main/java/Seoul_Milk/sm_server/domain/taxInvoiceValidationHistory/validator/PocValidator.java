package Seoul_Milk.sm_server.domain.taxInvoiceValidationHistory.validator;

import static Seoul_Milk.sm_server.global.exception.ErrorCode.INVALID_PARAMETER;

import Seoul_Milk.sm_server.global.exception.CustomException;
import org.springframework.stereotype.Component;

/**
 * searchByProviderOrConsumer에서 parameter로 사용될 poc데이터의 유효성 검사 클래스
 */
@Component
public class PocValidator {
    public void validate(String poc) {
        if (poc == null || poc.trim().isEmpty()) { // trim은 혹시나 " " 이런식으로 공백이 들어올까봐
            throw new CustomException(INVALID_PARAMETER);
        }
    }
}
