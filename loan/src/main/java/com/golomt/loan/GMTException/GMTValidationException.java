package com.golomt.loan.GMTException;

public class GMTValidationException extends GMTException {
    
    public GMTValidationException(String message) {
        super(message);
    }
    
    public GMTValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
