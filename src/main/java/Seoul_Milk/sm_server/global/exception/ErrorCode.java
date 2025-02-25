package Seoul_Milk.sm_server.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 에러코드 규약
     * HTTP Status Code는 에러에 가장 유사한 코드를 부여한다.
     * 사용자정의 에러코드는 중복되지 않게 배정한다.
     * 사용자정의 에러코드는 각 카테고리 이름과 숫자를 조합하여 명확성을 더한다.
     */

    /**
     * 400 : 잘못된 요청
     * 401 : 인증되지 않은 요청
     * 403 : 권한의 문제가 있을때
     * 404 : 요청한 리소스가 존재하지 않음
     * 409 : 현재 데이터와 값이 충돌날 때(ex. 아이디 중복)
     * 412 : 파라미터 값이 뭔가 누락됐거나 잘못 왔을 때
     * 422 : 파라미터 문법 오류
     * 424 : 뭔가 단계가 꼬였을때, 1번안하고 2번하고 그런경우
     */

    // Common
    SERVER_UNTRACKED_ERROR("COMMON500", "미등록 서버 에러입니다. 서버 팀에 연락주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("COMMON400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON401", "인증되지 않은 요청입니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON403", "권한이 부족합니다.", HttpStatus.FORBIDDEN),
    OBJECT_NOT_FOUND("COMMON404", "조회된 객체가 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PARAMETER("COMMON422", "잘못된 파라미터입니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    PARAMETER_VALIDATION_ERROR("COMMON422", "파라미터 검증 에러입니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    PARAMETER_GRAMMAR_ERROR("COMMON422", "파라미터 문법 에러입니다.", HttpStatus.UNPROCESSABLE_ENTITY),

    // Token
    TOKEN_INVALID("TOKEN401", "유효하지 않은 Token 입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID_ROLE("TOKEN401", "JWT 토큰에 Role 정보가 없습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED("TOKEN401", "Access Token 이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_INVALID("TOKEN401", "유효하지 않은 Access Token 입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("TOKEN404", "해당 사용자에 대한 Refresh Token 을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_MISMATCH("TOKEN401", "Refresh Token 이 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("TOKEN401", "Refresh Token 이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("TOKEN401", "유효하지 않은 Refresh Token 입니다.", HttpStatus.UNAUTHORIZED),

    // User (회원)
    USER_ALREADY_EXIST("USER400", "이미 회원가입된 유저입니다.", HttpStatus.BAD_REQUEST),
    USER_EMPLOYEE_ID_NOT_EXIST("USER404", "존재하지 않는 사번입니다.", HttpStatus.NOT_FOUND),
    USER_NOT_VALID("USER404", "유효한 사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_WRONG_PASSWORD("USER401", "비밀번호가 틀렸습니다.", HttpStatus.UNAUTHORIZED),
    USER_SAME_PASSWORD("USER400", "동일한 비밀번호로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST),
    USER_NO_PERMISSION("USER403", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    USER_FORBIDDEN("USER403", "유저의 권한이 부족합니다.", HttpStatus.FORBIDDEN),
    USER_NOT_IN_STUDY("USER404", "스터디에 속해있지 않은 회원입니다.", HttpStatus.NOT_FOUND),

    // 세금계산서 (TaxInvoice)
    TAX_INVOICE_NOT_EXIST("TAX_INVOICE404", "존재하지 않는 세금계산서입니다.", HttpStatus.NOT_FOUND),

    // UPLOAD (업로드)
    UPLOAD_FAILED("UPLOAD001", "업로드 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_FAILED("DELETE001", "삭제 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_URL("URL400", "유효하지 않은 파일 URL입니다.", HttpStatus.BAD_REQUEST),
    INVALID_URL_FORM("URL400", "URL 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // OCR
    OCR_EMPTY_JSON("OCR400", "OCR JSON 응답이 비어 있습니다.", HttpStatus.BAD_REQUEST),
    OCR_INVALID_JSON("OCR422", "OCR JSON 응답이 잘못된 JSON 형식입니다.", HttpStatus.BAD_REQUEST),
    OCR_NO_IMAGES("OCR404", "OCR 응답 JSON에서 images 필드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    OCR_NO_FIELDS("OCR404", "OCR 결과에 fields 데이터가 없습니다.", HttpStatus.NOT_FOUND),
    OCR_NO_RESULT("OCR404", "OCR API에서 반환된 결과가 없습니다.", HttpStatus.NOT_FOUND),
    INSUFFICIENT_REGISTRATION_NUMBERS("OCR422", "등록번호가 2개 미만입니다.", HttpStatus.UNPROCESSABLE_ENTITY),


    // JSON
    JSON_PARSING_FAILED("JSON001", "JSON 파싱에 실패했습니다.", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String message;
    private final HttpStatus status;
}