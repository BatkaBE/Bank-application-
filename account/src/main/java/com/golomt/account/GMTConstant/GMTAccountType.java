package com.golomt.account.GMTConstant;

public enum GMTAccountType {

    SAVINGS("SAVINGS"),
    CHECKING("CHEKING"),

    ;
    private String value;

    GMTAccountType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
