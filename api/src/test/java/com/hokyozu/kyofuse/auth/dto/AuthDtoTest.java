package com.hokyozu.kyofuse.auth.dto;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthMeResponse;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void registerRequestAcceptsValidPayload() {
        RegisterRequest request = new RegisterRequest(
                "John",
                "Doe",
                "john@example.com",
                "john",
                "password123"
        );

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void registerRequestRejectsBlankInvalidAndOversizedPayload() {
        RegisterRequest request = new RegisterRequest(
                "",
                "",
                "not-an-email",
                "",
                "short"
        );

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("firstName", "lastName", "email", "username", "password");
    }

    @Test
    void registerRequestRejectsInvalidUsernames() {
        // Slashes
        RegisterRequest requestWithSlashes = new RegisterRequest(
                "John", "Doe", "john@example.com", "///", "password123"
        );
        assertThat(validator.validate(requestWithSlashes))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");

        // Special characters
        RegisterRequest requestWithSpecialChars = new RegisterRequest(
                "John", "Doe", "john@example.com", "user@name!", "password123"
        );
        assertThat(validator.validate(requestWithSpecialChars))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");

        // Less than 3 characters
        RegisterRequest requestTooShort = new RegisterRequest(
                "John", "Doe", "john@example.com", "ab", "password123"
        );
        assertThat(validator.validate(requestTooShort))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");

        // Only underscores
        RegisterRequest requestOnlyUnderscores = new RegisterRequest(
                "John", "Doe", "john@example.com", "___", "password123"
        );
        assertThat(validator.validate(requestOnlyUnderscores))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");

        // Only numbers
        RegisterRequest requestOnlyNumbers = new RegisterRequest(
                "John", "Doe", "john@example.com", "12345", "password123"
        );
        assertThat(validator.validate(requestOnlyNumbers))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");

        // Only numbers and underscores
        RegisterRequest requestOnlyNumbersAndUnderscores = new RegisterRequest(
                "John", "Doe", "john@example.com", "_123_456_", "password123"
        );
        assertThat(validator.validate(requestOnlyNumbersAndUnderscores))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("username");

        // Valid username containing letters, numbers, and underscores
        RegisterRequest requestValidWithMix = new RegisterRequest(
                "John", "Doe", "john@example.com", "_user_123_", "password123"
        );
        assertThat(validator.validate(requestValidWithMix)).isEmpty();

        // Valid minimal username with letter
        RegisterRequest requestValidMinimal = new RegisterRequest(
                "John", "Doe", "john@example.com", "_a1", "password123"
        );
        assertThat(validator.validate(requestValidMinimal)).isEmpty();
    }

    @Test
    void loginRequestValidatesBlankLoginAndShortPassword() {
        LoginRequest request = new LoginRequest("", "short");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
                .contains("login", "password");
    }

    @Test
    void authResponsesExposeRecordValues() {
        UUID userId = UUID.randomUUID();

        AuthResponse authResponse = new AuthResponse(userId, "john@example.com", "john", "USER");
        AuthMeResponse meResponse = new AuthMeResponse(userId, "john@example.com", "john", "USER", true, "COMPLETED");

        assertThat(authResponse.userId()).isEqualTo(userId);
        assertThat(authResponse.email()).isEqualTo("john@example.com");
        assertThat(authResponse.username()).isEqualTo("john");
        assertThat(authResponse.role()).isEqualTo("USER");
        assertThat(meResponse.userId()).isEqualTo(userId);
        assertThat(meResponse.email()).isEqualTo("john@example.com");
        assertThat(meResponse.username()).isEqualTo("john");
        assertThat(meResponse.role()).isEqualTo("USER");
        assertThat(meResponse.totpEnabled()).isTrue();
        assertThat(meResponse.profileSetupStatus()).isEqualTo("COMPLETED");
    }
}
