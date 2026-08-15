package com.hokyozu.kyofuse.communities.dto.request;

import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCommunityRequest(
        @Size(max = 80)
        String communityName,

        @Size(max = 100)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain only lowercase letters, numbers and single hyphens")
        String communitySlug,

        @Size(max = 500)
        String communityDescription,

        @Size(max = 500)
        String communityAvatarUrl,

        @Size(max = 500)
        String communityBannerUrl,

        CommunityVisibility visibility
) {
}
