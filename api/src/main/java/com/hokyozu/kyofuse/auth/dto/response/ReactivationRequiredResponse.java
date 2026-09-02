package com.hokyozu.kyofuse.auth.dto.response;

import java.time.Instant;

public record ReactivationRequiredResponse(
        boolean reactivationRequired,
        String reactivationToken,
        String maskedEmail,
        boolean scheduledDeletion,
        Instant scheduledDeletionDate
) {
    public ReactivationRequiredResponse(String reactivationToken, String maskedEmail, boolean scheduledDeletion, Instant scheduledDeletionDate) {
        this(true, reactivationToken, maskedEmail, scheduledDeletion, scheduledDeletionDate);
    }
}
