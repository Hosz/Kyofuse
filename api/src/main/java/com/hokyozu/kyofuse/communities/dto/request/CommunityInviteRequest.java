package com.hokyozu.kyofuse.communities.dto.request;

import jakarta.validation.constraints.Size;

public record CommunityInviteRequest(
        @Size(max = 500, message = "A mensagem não pode exceder 500 caracteres.")
        String message
) {
}
