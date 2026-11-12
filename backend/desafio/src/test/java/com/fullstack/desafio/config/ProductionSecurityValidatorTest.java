package com.fullstack.desafio.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductionSecurityValidatorTest {

    @Test
    void rejectsDevelopmentCredentialsAndCookieSettings() {
        ProductionSecurityValidator validator = validator(
                "dev-only-secret-change-me-0123456789abcdef",
                "admin@teste.com",
                "Admin@123",
                false,
                "None");

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void rejectsCookiesWithoutSecureFlagInProduction() {
        ProductionSecurityValidator validator = validator(
                "production-secret-with-at-least-32-bytes",
                "admin@company.example",
                "different-strong-password",
                false,
                "None");

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void acceptsExplicitProductionSettings() {
        ProductionSecurityValidator validator = validator(
                "production-secret-with-at-least-32-bytes",
                "admin@company.example",
                "different-strong-password",
                true,
                "None");

        assertDoesNotThrow(validator::validate);
    }

    private ProductionSecurityValidator validator(
            String secret, String email, String password, boolean secureCookie, String sameSite) {
        ProductionSecurityValidator validator = new ProductionSecurityValidator();
        ReflectionTestUtils.setField(validator, "jwtSecret", secret);
        ReflectionTestUtils.setField(validator, "adminEmail", email);
        ReflectionTestUtils.setField(validator, "adminName", "Production Admin");
        ReflectionTestUtils.setField(validator, "adminPassword", password);
        ReflectionTestUtils.setField(validator, "secureCookie", secureCookie);
        ReflectionTestUtils.setField(validator, "sameSite", sameSite);
        ReflectionTestUtils.setField(validator, "allowedOrigins", "https://app.example.com");
        ReflectionTestUtils.setField(validator, "databaseUrl", "jdbc:postgresql://db.example.com:5432/fullstack");
        ReflectionTestUtils.setField(validator, "databaseUsername", "production-user");
        ReflectionTestUtils.setField(validator, "databasePassword", "production-db-password");
        return validator;
    }
}
