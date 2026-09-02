package com.hokyozu.kyofuse.posts.validator;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class PostValidator {
    public void validate(CreatePostRequest request) {
        boolean hasContent = request.content() != null && !request.content().trim().isEmpty();
        boolean hasMedia = request.media() != null && !request.media().isEmpty();

        if (!hasContent && !hasMedia) {
            throw new BadRequestException("Post must contain text content or at least one media attachment");
        }

        if (request.visibility() == PostVisibility.TEAM_ONLY) {
            throw new BadRequestException("TEAM_ONLY posts are not available in V1");
        }
    }
}
