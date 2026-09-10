package com.hokyozu.kyofuse.communities.dto.request;

import jakarta.validation.constraints.Size;

public record CommunityInviteCancelRequest(
        @Size(max = 500, message = "O motivo do cancelamento não pode exceder 500 caracteres.")
        String cancellationReason
) {
}
