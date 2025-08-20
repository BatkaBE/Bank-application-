package com.golomt.transaction.GMTConstant;

public enum GMTCurrencyConstant {
    MNT("MNT"),
    USD("USD"),
    JPY("JPY"),
    EUR("EUR")
            ;
    private String value;
    GMTCurrencyConstant(String value) {
        this.value = value;
    }
    public String value() { return value; }
}
