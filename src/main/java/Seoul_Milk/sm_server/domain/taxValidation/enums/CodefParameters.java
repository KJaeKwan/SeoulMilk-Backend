package Seoul_Milk.sm_server.domain.taxValidation.enums;

public enum CodefParameters {
    ORGANIZATION("organization"),
    ID("id"),
    LOGIN_TYPE("loginType"),
    LOGIN_TYPE_LEVEL("loginTypeLevel"),
    USER_NAME("userName"),
    PHONE_NO("phoneNo"),
    IDENTITY("identity"),
    TELECOM("telecom"),
    SUPPLIER_REG_NUMBER("supplierRegNumber"),
    CONTRACTOR_REG_NUMBER("contractorRegNumber"),
    APPROVAL_NO("approvalNo"),
    REPORTING_DATE("reportingDate"),
    SUPPLY_VALUE("supplyValue"),
    TWO_WAY_INFO("twoWayInfo"),
    ORIGINAL_APPROVAL_NO("originalApprovalNo");

    private final String key;

    CodefParameters(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
