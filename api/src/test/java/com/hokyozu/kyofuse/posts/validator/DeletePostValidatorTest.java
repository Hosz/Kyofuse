package com.hokyozu.kyofuse.posts.validator;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostStatus;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeletePostValidatorTest {

    private final DeletePostValidator validator = new DeletePostValidator();

    @Test
    void validatePassesWhenRequesterIsAuthorAndPostIsNotDeleted() {
        UUID authorId = UUID.randomUUID();
        Post post = post(authorId, PostStatus.ACTIVE);

        assertThatCode(() -> validator.validate(post, authorId)).doesNotThrowAnyException();
    }

    @Test
    void validateThrowsUnauthorizedWhenRequesterIsNotAuthor() {
        Post post = post(UUID.randomUUID(), PostStatus.ACTIVE);

        assertThatThrownBy(() -> validator.validate(post, UUID.randomUUID()))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("User is not the author of the post and cannot delete it");
    }

    @Test
    void validateThrowsBadRequestWhenPostIsAlreadyDeleted() {
        UUID authorId = UUID.randomUUID();
        Post post = post(authorId, PostStatus.DELETED);

        assertThatThrownBy(() -> validator.validate(post, authorId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Post has been already deleted");
    }

    private static Post post(UUID authorId, PostStatus status) {
        return Post.builder()
                .author(User.builder().id(authorId).build())
                .status(status)
                .build();
    }
}
