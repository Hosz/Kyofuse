package com.hokyozu.kyofuse.shared.exception;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseDtoTest {

    @Test
    void apiErrorResponseExposesRecordValues() {
        Instant now = Instant.now();

        ApiErrorResponse response = new ApiErrorResponse(now, 400, "Bad Request", "Invalid request");

        assertThat(response.timestamp()).isEqualTo(now);
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.error()).isEqualTo("Bad Request");
        assertThat(response.message()).isEqualTo("Invalid request");
    }

    @Test
    void validationErrorResponseExposesRecordValues() {
        Instant now = Instant.now();
        Map<String, String> fields = Map.of("email", "Email inválido.");

        ValidationErrorResponse response = new ValidationErrorResponse(
                now,
                400,
                "Validation Error",
                "Dados inválidos.",
                fields
        );

        assertThat(response.timestamp()).isEqualTo(now);
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.fields()).containsEntry("email", "Email inválido.");
    }
}
