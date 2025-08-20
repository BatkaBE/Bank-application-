package com.golomt.auth.GMTConstant;

public enum GMTException {

    VALIDATION("Validation"),
    CUSTOM("Custom"),
    RUN_TIME("Run time error"),

    NOT_FOUNT("Path not fount"),

    METHOD_NOT_ALLOWED("Method not allowed"),

    API_PARAM("Api param"),

    JSON_PARSE("Json parse error");

    private final String value;

    GMTException(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    public static class ErrorCodes {
        public static final String INTERNAL_SERVER_ERROR = "500";
        public static final String BAD_REQUEST = "400";
    }
}
