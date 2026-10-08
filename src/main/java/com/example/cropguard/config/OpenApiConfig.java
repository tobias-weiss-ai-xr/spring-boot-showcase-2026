package com.example.cropguard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Live API docs at /swagger-ui and published contract at /v3/api-docs (springdoc-openapi). Public in the demo — see arc42 08. */
@Configuration
public class OpenApiConfig {

    /** Module-level API tags (policy/billing/claims) exposed in the published contract. */
    static final List<Tag> MODULE_TAGS = List.of(
        new Tag().name("policy").description("Versicherungsverträge"),
        new Tag().name("billing").description("Prämien und Quotes"),
        new Tag().name("claims").description("Schadenfälle"));

    @Bean
    public OpenAPI cropGuardOpenApi(
            @Value("${spring.application.version:1.0.0}") String buildVersion) {
        return new OpenAPI()
            .info(new Info()
                .title("CropGuard API")
                .description("Hagelversicherung Demo-API: Versicherungen, Schadenfälle, DWD-Risikodaten.")
                .version(buildVersion))
            .tags(MODULE_TAGS);
    }
}
