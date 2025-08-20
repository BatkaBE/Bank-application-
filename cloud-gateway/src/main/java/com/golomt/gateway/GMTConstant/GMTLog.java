package com.golomt.gateway.GMTConstant;

public enum GMTLog {
    ZUUL("LOG.ZUUl"),
    UTILITY("LOG.UTILITY");
    private final String value;

    GMTLog(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
