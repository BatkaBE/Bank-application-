package com.golomt.loan.GMTException;

public class GMTRuntimeException extends GMTException {
    
    public GMTRuntimeException(String message) {
        super(message);
    }
    
    public GMTCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
