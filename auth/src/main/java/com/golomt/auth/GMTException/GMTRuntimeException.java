package com.golomt.auth.GMTException;

public class GMTRuntimeException extends RuntimeException {
    public GMTRuntimeException(String message) {
        super(message);
    }

    public GMTRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public GMTRuntimeException(Throwable cause) {
        super(cause);
    }

    public GMTRuntimeException() {
        super();
    }

    public GMTRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
