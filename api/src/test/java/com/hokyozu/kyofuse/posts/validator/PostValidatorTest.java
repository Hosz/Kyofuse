package com.hokyozu.kyofuse.posts.validator;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostValidatorTest {

    private final PostValidator validator = new PostValidator();

    @Test
    void validatePassesWhenVisibilityIsPublic() {
        CreatePostRequest request = new CreatePostRequest("content", PostType.TEXT, PostVisibility.PUBLIC);

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validateThrowsWhenVisibilityIsTeamOnly() {
        CreatePostRequest request = new CreatePostRequest("content", PostType.TEXT, PostVisibility.TEAM_ONLY);

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("TEAM_ONLY posts are not available in V1");
    }
}
