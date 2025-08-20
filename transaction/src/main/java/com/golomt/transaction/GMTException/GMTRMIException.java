package com.golomt.transaction.GMTException;

public class GMTRMIException extends RuntimeException {
    public GMTRMIException(String message) {
        super(message);
    }

    public GMTRMIException(String message, Throwable cause) {
        super(message, cause);
    }

    public GMTRMIException(Throwable cause) {
        super(cause);
    }

    public GMTRMIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
