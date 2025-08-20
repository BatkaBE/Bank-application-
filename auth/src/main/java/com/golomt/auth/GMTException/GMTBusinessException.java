package com.golomt.auth.GMTException;

import java.util.List;

public class GMTBusinessException extends Exception {

    private List<String> errors;
    public GMTBusinessException(String message) {
        super(message);
    }
    public GMTBusinessException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
    public GMTBusinessException(String message, List<String> errors, Throwable cause) {
        super(message, cause);
        this.errors = errors;
    }
    public GMTBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    public GMTBusinessException(List<String> errors) {
        this.errors = errors;
    }
    public GMTBusinessException(List<String> errors, Throwable cause) {
        super(cause);
        this.errors = errors;
    }
    public GMTBusinessException( Throwable cause) {
        super(cause);
    }

    public List<String> getErrors() {return this.errors;}

}
