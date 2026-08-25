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
        AuthMeResponse meResponse = new AuthMeResponse(userId, "john@example.com", "john", "USER");

        assertThat(authResponse.userId()).isEqualTo(userId);
        assertThat(authResponse.email()).isEqualTo("john@example.com");
        assertThat(authResponse.username()).isEqualTo("john");
        assertThat(authResponse.role()).isEqualTo("USER");
        assertThat(meResponse.userId()).isEqualTo(userId);
        assertThat(meResponse.email()).isEqualTo("john@example.com");
        assertThat(meResponse.username()).isEqualTo("john");
        assertThat(meResponse.role()).isEqualTo("USER");
    }
}
