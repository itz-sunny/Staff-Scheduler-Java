package com.staffscheduler.aspect;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.security.Principal;
import java.util.Map;

@Singleton
public class MDCLoggingInterceptor implements MethodInterceptor<Object, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MDCLoggingInterceptor.class);

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Map<String, Object> paramValueMap = context.getParameterValueMap();
        ExecutableMethod<?, ?> executableMethod = context.getExecutableMethod();
        String methodName = executableMethod.getMethodName();
        if (paramValueMap.get("principal") != null) {
            Principal principal = (Principal) paramValueMap.get("principal");
            MDC.put("UserId", "UserId: " + principal.getName());
        }


        Object toRet;
        try {
            toRet = context.proceed();
            LOGGER.info("request received for operation: {} for params: {}", methodName, paramValueMap);
        } catch (Exception e) {
            LOGGER.error("Exception: {} in performing operation: {} for params: {}",
                    e.getClass() + "-" + e.getMessage(), methodName, paramValueMap);
            throw e;
        }
        LOGGER.info("response sent for operation: {} for params: {} with response {}", methodName, paramValueMap, toRet);
        MDC.clear();
        return toRet;
    }
}
