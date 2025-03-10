package Seoul_Milk.sm_server.domain.taxInvoiceValidation.enums;

import java.util.Objects;

public enum CodefResponseCode {
    SUCCESS_RESPONSE("CF-00000", "성공적인 요청"),
    NEED_SIMPLE_AUTHENTICATION("CF-03002", "간편인증 필요"),
    INVALID_APPROVE_NUM("CF-13361", "잘못된 승인번호입니다."),
    ERROR_APPROVE_NUM("CF-13364", "승인번호 관련 오류입니다.");
    private final String code;
    private final String message;
    CodefResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public boolean isEqual(String code){
        return Objects.equals(this.code, code);
    }
}
