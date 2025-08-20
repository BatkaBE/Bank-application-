package com.golomt.account.GMTException;

public class GMTValidationException extends Exception {
    public GMTValidationException(String message) {
        super(message);
    }

    public GMTValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GMTValidationException(Throwable cause) {
        super(cause);
    }

    public GMTValidationException() {
        super();
    }

    public GMTValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
