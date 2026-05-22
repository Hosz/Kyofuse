package com.hokyozu.kyofuse.posts.validator;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class PostValidator {
    public void validate(CreatePostRequest request) {
        if (request.visibility() == PostVisibility.TEAM_ONLY) {
            throw new BadRequestException("TEAM_ONLY posts are not available in V1");
        }
    }
}
