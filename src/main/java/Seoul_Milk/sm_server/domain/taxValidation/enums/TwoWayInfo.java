package Seoul_Milk.sm_server.domain.taxValidation.enums;

public enum TwoWayInfo {
    JOB_INDEX("JOB_INDEX"),
    THREAD_INDEX("THREAD_INDEX"),
    JTI("JTI"),
    TWO_WAY_TIMESTAMP("TWO_WAY_TIMESTAMP");

    private final String parameter;

    TwoWayInfo(String parameter) {
        this.parameter = parameter;
    }

    /**
     * 대문자 형식인 enum내용 카멜표기식으로 바꾸기
     * @return
     */
    public String setCarmelCase(){
        String[] parts = this.parameter.toLowerCase().split("_");
        StringBuilder camelCaseString = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(parts[i].substring(0, 1).toUpperCase()) // 첫 글자 대문자 변환
                    .append(parts[i].substring(1));
        }
        return camelCaseString.toString();
    }
}
