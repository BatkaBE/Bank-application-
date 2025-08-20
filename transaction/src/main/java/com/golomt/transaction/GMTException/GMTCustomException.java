package com.golomt.transaction.GMTException;

import org.springframework.http.HttpStatus;

public class GMTCustomException extends Exception {
    private final HttpStatus httpStatus;

    public GMTCustomException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST; // default
    }

    public GMTCustomException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}