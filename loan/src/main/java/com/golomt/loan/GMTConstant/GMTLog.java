package com.golomt.loan.GMTConstant;

public enum GMTLog {
    LOAN("LOAN");

    private final String value;

    GMTLog(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
