package com.staffscheduler.exception.handler;

import com.staffscheduler.exception.DuplicateUserException;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.*;
import io.micronaut.security.authentication.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class GenericExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionHandler.class);

    protected void logMessage(HttpRequest<?> request, HttpResponse<?> response) {
        StringBuilder sblog = new StringBuilder();
        String httpMethod = request.getMethod().name();
        String path = request.getPath();
        Integer responseCode = -1;
        if (response != null) {
            responseCode = response.getStatus().getCode();
        }

        Optional<Object> authOpt = request.getAttribute(HttpAttributes.PRINCIPAL.toString());
        if (authOpt.isPresent()) {
            Authentication auth = (Authentication) authOpt.get();
            sblog.append("Auth/Customer: ").append(auth.getAttributes().get("sub"));
        }

        String sessionId = request.getHeaders().get(HttpHeaders.AUTHORIZATION_INFO);
        if (StringUtils.isNotEmpty(sessionId)) {
            sblog.append(",").append(" sessionId: ").append(sessionId);
        }

        sblog.append(",").append(" sourceIp: ").append(request.getHeaders().get("X-Forwarded-For"));
        sblog.append(",").append(" method: ").append(httpMethod).append(" ").append(path);
        sblog.append(",").append(" responseCode: ").append(responseCode);
        LOGGER.info("Canonical Logline {}", sblog.toString());

    }

    private void updateSecurityHeaders(HttpResponse<?> httpResponse) {
        MutableHttpResponse<?> response = (MutableHttpResponse<?>) httpResponse;
        response.header("Cache-Control", "no-cache,no-store");
        response.header("X-Content-Type-Options", "nosniff");
        response.header("X-Frame-Options", "deny");
        response.header("X-XSS-Protection", "1; mode=block");
    }
}
