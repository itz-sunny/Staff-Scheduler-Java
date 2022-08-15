package com.staffscheduler.exception;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }

    public String getMessage() {
        return super.getMessage();
    }
}
