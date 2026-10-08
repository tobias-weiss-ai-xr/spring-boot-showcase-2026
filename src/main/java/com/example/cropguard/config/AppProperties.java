package com.example.cropguard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cropguard")
public record AppProperties(
    String jwtSecret,
    long jwtExpirationSeconds,
    String appName,
    LegacyImport legacyImport
) {
    /** Legacy data import switch. */
    public record LegacyImport(boolean enabled) {}
}
