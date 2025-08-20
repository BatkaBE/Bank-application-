package com.golomt.auth.GMTConstant;

public enum GMTCommon {
    FLAG_Y("Y"),
    FLAG_N("N"),
    FATAL("FATAL"),
    ERROR("ERROR"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    SUCCESS_FLAG("S"),
    FAILURE_FLAG("F"),
    ;
    private String value;
    GMTCommon(String value) {
        this.value = value;
    }
    public String value() { return value; }
}
