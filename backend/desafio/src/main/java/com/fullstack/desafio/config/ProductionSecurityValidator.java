package com.fullstack.desafio.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("prod")
public class ProductionSecurityValidator {

    private static final String DEVELOPMENT_JWT_SECRET =
            "dev-only-secret-change-me-0123456789abcdef";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.auth.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.auth.cookie.same-site}")
    private String sameSite;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @PostConstruct
    void validate() {
        if (DEVELOPMENT_JWT_SECRET.equals(jwtSecret)) {
            throw new IllegalStateException("Configure um JWT_SECRET exclusivo no perfil prod.");
        }
        if (adminName.isBlank() || adminEmail.isBlank() || adminPassword.isBlank()
                || "admin@teste.com".equalsIgnoreCase(adminEmail.trim())
                || "Admin@123".equals(adminPassword.trim())) {
            throw new IllegalStateException("Configure credenciais ADMIN exclusivas no perfil prod.");
        }
        String normalizedOrigins = allowedOrigins.toLowerCase(java.util.Locale.ROOT);
        if (allowedOrigins.isBlank() || normalizedOrigins.contains("*")
                || normalizedOrigins.contains("localhost")
                || normalizedOrigins.contains("127.0.0.1") || databaseUrl.isBlank()
                || databaseUrl.contains("localhost")
                || databaseUsername.isBlank() || "fullstack".equals(databaseUsername)
                || databasePassword.isBlank() || "fullstack".equals(databasePassword)) {
            throw new IllegalStateException(
                    "Configure credenciais do banco e CORS_ALLOWED_ORIGINS exclusivos no perfil prod.");
        }
        if (!secureCookie) {
            throw new IllegalStateException("O cookie de autenticação deve usar Secure no perfil prod.");
        }
        if (!Set.of("Strict", "Lax", "None").contains(sameSite)) {
            throw new IllegalStateException("app.auth.cookie.same-site deve ser Strict, Lax ou None.");
        }
    }
}
