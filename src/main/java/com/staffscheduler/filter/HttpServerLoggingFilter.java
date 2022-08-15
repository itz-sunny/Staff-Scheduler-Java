package com.staffscheduler.filter;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.security.authentication.Authentication;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Filter("/**")
public class HttpServerLoggingFilter implements HttpServerFilter {

    private static final ExecutorService loggerTaskExecutor = Executors.newFixedThreadPool(5);
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServerLoggingFilter.class);

    @Override
    public String toString() {
        return "HttpServerLoggingFilter{}";
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        long requestTime = System.currentTimeMillis();
        return Publishers.map(chain.proceed(request), response -> {
            logMessage(request, response, requestTime);
            updateSecurityHeaders(response);
            return response;
        });
    }

    private void logMessage(HttpRequest<?> request, HttpResponse<?> response, long requestTime) {
        StringBuilder sblog = new StringBuilder();
        String httpMethod = request.getMethod().name();
        String path = request.getPath();
        Integer responseCode = -1;
        if (response != null) {
            responseCode = response.getStatus().getCode();
        }
        long responseTime = System.currentTimeMillis();
        long duration = responseTime - requestTime;

        Optional<Object> authOpt = request.getAttribute(HttpAttributes.PRINCIPAL.toString());
        if (authOpt.isPresent()) {
            Authentication auth = (Authentication) authOpt.get();
            sblog.append("Auth/Customer: ").append(auth.getAttributes().get("sub"));
        }

        sblog.append(",").append(" sourceIp: ").append(request.getHeaders().get("X-Forwarded-For"));
        sblog.append(",").append(" method: ").append(httpMethod).append(" ").append(path);
        sblog.append(",").append(" responseCode: ").append(responseCode);
        sblog.append(",").append(" duration: ").append(duration).append("ms");
        LOGGER.info(String.format("Canonical Logline=%s", sblog.toString()), HttpServerLoggingFilter.class);

    }

    private void updateSecurityHeaders(HttpResponse<?> httpResponse) {
        MutableHttpResponse<?> response = (MutableHttpResponse<?>) httpResponse;
        response.header("Cache-Control", "no-cache");
        response.header("X-Content-Type-Options", "nosniff");
        response.header("X-Frame-Options", "deny");
        response.header("X-XSS-Protection", "1; mode=block");
        response.header("Strict-Transport-Security", "max-age=86400; includeSubDomains");
    }

    @Override
    public int getOrder() {
       return Ordered.LOWEST_PRECEDENCE - 1;
    }

}