package Seoul_Milk.sm_server.domain.taxValidation.enums;

import java.util.Objects;

public enum CodefResponseCode {
    SUCCESS_RESPONSE("CF-00000", "성공적인 요청"),
    NEED_SIMPLE_AUTHENTICATION("CF-03002", "간편인증 필요");
    private final String code;
    private final String message;
    CodefResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    public boolean is_success(String code){
        return Objects.equals(SUCCESS_RESPONSE.code, code);
    }
}
