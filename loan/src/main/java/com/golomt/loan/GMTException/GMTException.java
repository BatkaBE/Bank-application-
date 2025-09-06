package com.golomt.loan.GMTException;

public class GMTException extends Exception {
    
    public GMTException(String message) {
        super(message);
    }
    
    public GMTException(String message, Throwable cause) {
        super(message, cause);
    }
}
