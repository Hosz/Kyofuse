package com.hokyozu.kyofuse.invites.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeamInviteCancelRequest(
        @NotBlank
        @NotNull
        @Size(max = 40)
        String cancellationReason
) {
}
