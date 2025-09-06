package main.java.com.golomt.loan.GMTConstant;

public enum GMTLoanStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED"),
    DEFAULTED("DEFAULTED"),
    CANCELLED("CANCELLED");

    private final String value;

    GMTLoanStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
