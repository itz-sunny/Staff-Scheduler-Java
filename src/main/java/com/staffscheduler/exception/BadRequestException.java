package com.staffscheduler.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String errorCode, String errorMessage){
        super(String.format("[ErrorCode=%s,ErrorMessage=%s", errorCode, errorMessage));
    }

    public BadRequestException(){}
}
