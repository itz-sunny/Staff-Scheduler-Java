package com.staffscheduler.exception;


public class InvalidUserException extends RuntimeException {

    public InvalidUserException(String message){

        super(message);

    }

    @Override
    public String getMessage() {

        return super.getMessage();
    }
}
