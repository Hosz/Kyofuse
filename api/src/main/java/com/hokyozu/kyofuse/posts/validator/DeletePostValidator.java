package com.hokyozu.kyofuse.posts.validator;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DeletePostValidator {

    public void validate(Post post, UUID userId) {

        if (!post.getAuthor().getId().equals(userId)) {
            throw new UnauthorizedException("User is not the author of the post and cannot delete it");
        }

        if (post.getStatus().equals(PostStatus.DELETED)) {
            throw new BadRequestException("Post has been already deleted");
        }
    }
}
