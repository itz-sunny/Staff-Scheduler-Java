package com.staffscheduler;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;

@OpenAPIDefinition(
        info = @Info(
                title = "Staff Scheduler API",
                version = "0.1",
                description = "The Staff-Scheduler APIs are based on REST. Our API has predictable resource-oriented URLs, " +
                        "accepts JSON-encoded request bodies, returns JSON-encoded responses, and uses standard HTTP " +
                        "response codes, authentication, and verbs. <p>Contact us for API Key</p>"
        )
)
@SecuritySchemes({
        @SecurityScheme(name = "BearerAuth",
            type = SecuritySchemeType.HTTP,
            scheme = "bearer",
            bearerFormat = "jwt",
            description = "      ")
})
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
