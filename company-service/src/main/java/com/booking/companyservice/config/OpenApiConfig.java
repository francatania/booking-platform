package com.booking.companyservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Company Service",
        version = "1.0.0",
        description = "Manages companies and their service catalog."
    ),
    security = @SecurityRequirement(name = "Bearer")
)
@SecurityScheme(
    name = "Bearer",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OperationCustomizer globalParameters() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            operation.addParametersItem(
                new Parameter()
                    .in("header")
                    .name("Accept-Language")
                    .description("Language for error messages")
                    .required(false)
                    .schema(new StringSchema()._enum(List.of("en", "es")).example("es"))
            );
            return operation;
        };
    }
}
