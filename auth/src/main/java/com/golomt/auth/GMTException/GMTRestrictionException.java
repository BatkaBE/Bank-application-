package com.golomt.auth.GMTException;

public class GMTRestrictionException extends Exception {
    public GMTRestrictionException(String message) {
        super(message);
    }

    public GMTRestrictionException(String message, Throwable cause) {
        super(message, cause);
    }

    public GMTRestrictionException(Throwable cause) {
        super(cause);
    }

    public GMTRestrictionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    }
}