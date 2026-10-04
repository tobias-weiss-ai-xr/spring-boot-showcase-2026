package com.example.cropguard;

import com.example.cropguard.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CropGuard — Hail Insurance Management System.
 *
 * Domain-specific demo for an agricultural hail insurance provider.
 * Covers all 10 Spring Boot course modules with real business logic:
 *   - Bundesland-based risk zones (German geography)
 *   - Crop type catalog with risk categories
 *   - Premium calculation algorithm (base rate × hectares × risk × deductible)
 *   - Hail event tracking and batch claim processing
 *   - German-language (DE)
 *   - Seasonal coverage windows
 *   - Damage assessment workflow with payout calculation
 *   - EUR currency throughout
 */
@SpringBootApplication
@org.springframework.boot.context.properties.EnableConfigurationProperties(AppProperties.class)
public class CropGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(CropGuardApplication.class, args);
    }
}
