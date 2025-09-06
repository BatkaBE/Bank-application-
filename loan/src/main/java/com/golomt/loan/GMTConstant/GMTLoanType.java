package com.golomt.loan.GMTConstant;

public enum GMTLoanType {
    PERSONAL("PERSONAL"),
    BUSINESS("BUSINESS"),
    MORTGAGE("MORTGAGE"),
    AUTO("AUTO"),
    STUDENT("STUDENT"),
    CONSUMER("CONSUMER");

    private final String value;

    GMTLoanType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

