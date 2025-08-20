package com.golomt.transaction.GMTConstant;

public enum GMTLog {
    TRANSACTION("LOG.TRANSACTION"),
    UTILITY("LOG.UTILITY");
    private final String value;

    GMTLog(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
