package com.staffscheduler.exception.handler;

import com.staffscheduler.exception.BadRequestException;
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
public class BadRequestExceptionHandler extends GenericExceptionHandler implements ExceptionHandler<BadRequestException, HttpResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadRequestExceptionHandler.class);

    @Override
    public HttpResponse handle(HttpRequest request, BadRequestException exception) {
        LOGGER.error("Bad request error message {}", exception.getMessage());
        JsonError error = new JsonError("Invalid Request");
        error.link(Link.SELF, Link.of(request.getUri()));
        logMessage(request, HttpResponse.status(HttpStatus.BAD_REQUEST).body(error));
        return HttpResponse.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
