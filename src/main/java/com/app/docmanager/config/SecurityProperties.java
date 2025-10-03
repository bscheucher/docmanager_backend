package com.app.docmanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityProperties {

    private boolean h2ConsoleEnabled = true;
    private boolean swaggerEnabled = true;
    private CorsProperties cors = new CorsProperties();

    @Data
    public static class CorsProperties {
        private String allowedOrigins = "http://localhost:3000,http://localhost:4200";
        private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        private String allowedHeaders = "*";
        private boolean allowCredentials = true;
        private long maxAge = 3600L;
    }
}