package com.golomt.loan.GMTException;

public class GMTCustomException extends GMTException {
    
    public GMTCustomException(String message) {
        super(message);
    }
    
    public GMTCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
