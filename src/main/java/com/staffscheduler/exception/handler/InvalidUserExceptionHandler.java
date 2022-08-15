package com.staffscheduler.exception.handler;

import com.staffscheduler.exception.InvalidUserException;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Produces
@Singleton
@Primary
public class InvalidUserExceptionHandler extends GenericExceptionHandler implements ExceptionHandler<InvalidUserException, HttpResponse<?>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidUserExceptionHandler.class);

    @Override
    public HttpResponse<?> handle(HttpRequest request, InvalidUserException exception) {
        LOGGER.error("Invalid user message: {}", exception.getMessage());
        JsonError error = new JsonError(exception.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));
        logMessage(request, HttpResponse.status(HttpStatus.BAD_REQUEST).body(error));
        return HttpResponse.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
