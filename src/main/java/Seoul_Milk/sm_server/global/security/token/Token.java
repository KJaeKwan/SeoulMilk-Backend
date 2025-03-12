package Seoul_Milk.sm_server.global.security.token;

/**
 * 토큰 관련(만료기한, 토큰 이름 등) 정보를 담은 상수 클래스입니다.
 */
public enum Token {

    // 첫 번째 인자 : token category 이름, 두 번째 인자 : 만료시간
    ACCESS_TOKEN("access", 86400000L), // 만료시간 24시간 //TODO 최종 제출 전 반드시 10분으로 되돌려 놓기!
    REFRESH_TOKEN("refresh", 86400000L); // 만료시간 24시간

    private final String category;
    private final Long expireMs;

    Token(String category, Long expireMs) {
        this.category = category;
        this.expireMs = expireMs;
    }

    public String category(){
        return category;
    }

    public Long expireMs(){
        return expireMs;
    }
}
