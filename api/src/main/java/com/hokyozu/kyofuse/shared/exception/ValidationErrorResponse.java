package com.hokyozu.kyofuse.shared.exception;

import java.time.Instant;
import java.util.Map;

public record ValidationErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fields
) {
}
