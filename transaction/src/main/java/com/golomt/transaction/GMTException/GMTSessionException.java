package com.golomt.transaction.GMTException;

public class GMTSessionException extends RuntimeException {
    public GMTSessionException(String message) {
        super(message);
    }

    public GMTSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public GMTSessionException(Throwable cause) {
        super(cause);
    }

    public GMTSessionException() {
        super();
    }

    public GMTSessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
