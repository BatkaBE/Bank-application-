package com.golomt.account.GMTConstant;

public enum GMTLog {
    ACCOUNT("LOG.ACCOUNT"),
    UTILITY("LOG.UTILITY");
    private final String value;

    GMTLog(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
