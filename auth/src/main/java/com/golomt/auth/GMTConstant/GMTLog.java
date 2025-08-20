package com.golomt.auth.GMTConstant;

public enum GMTLog {
    AUTH("LOG.AUTH"),
    UTILITY("LOG.UTILITY");
    private final String value;

    GMTLog(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
