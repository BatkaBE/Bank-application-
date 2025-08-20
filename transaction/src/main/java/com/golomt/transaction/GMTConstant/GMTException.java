package com.golomt.transaction.GMTConstant;

public enum GMTException {

    VALIDATION("Validation"),

    CUSTOM("Custom"),

    RUN_TIME("Run time error"),

    NOT_FOUND("Path not found"),

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
}
