package com.example.cropguard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Live API docs at /swagger-ui (springdoc-openapi). Public in the demo — see arc42 08. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cropGuardOpenApi() {
        return new OpenAPI().info(new Info()
            .title("CropGuard API")
            .description("Hagelversicherung Demo-API: Versicherungen, Schadenfälle, DWD-Risikodaten.")
            .version("v1"));
    }
}
