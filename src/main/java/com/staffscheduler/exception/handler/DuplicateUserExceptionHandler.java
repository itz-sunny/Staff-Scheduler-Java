package com.staffscheduler.exception.handler;

import com.staffscheduler.exception.DuplicateUserException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicateUserExceptionHandler extends GenericExceptionHandler implements ExceptionHandler<DuplicateUserException, HttpResponse<?>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(DuplicateUserExceptionHandler.class);

    @Override
    public HttpResponse<?> handle(HttpRequest request, DuplicateUserException exception) {
        LOGGER.error("Duplicate user message: {}", exception.getMessage());
        JsonError error = new JsonError(exception.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));
        logMessage(request, HttpResponse.status(HttpStatus.BAD_REQUEST).body(error));
        return HttpResponse.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
