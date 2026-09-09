package com.hokyozu.kyofuse.communities.dto.request;

import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import jakarta.validation.constraints.NotNull;

public record UpdateCommunityMemberRoleRequest(
        @NotNull(message = "O papel do membro é obrigatório.")
        CommunityMemberRole role
) {
}
