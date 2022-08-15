package com.staffscheduler.exception;


public class DBOperationException extends RuntimeException {

    public DBOperationException(String message){
        super(message);
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return "An error occurred while performing DB operation error=" + message;
    }
}
