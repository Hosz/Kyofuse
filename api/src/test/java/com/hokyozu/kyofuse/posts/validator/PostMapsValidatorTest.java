package com.hokyozu.kyofuse.posts.validator;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.posts.enums.PostType;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.profiles.enums.Cs2Map;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostMapsValidatorTest {

    private final PostMapsValidator validator = new PostMapsValidator();

    @Test
    void validatePassesWhenMapsAreNull() {
        CreatePostRequest request = new CreatePostRequest("content", PostType.TEXT, PostVisibility.PUBLIC, null);

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validatePassesWhenMapsAreUnique() {
        CreatePostRequest request = new CreatePostRequest(
                "content",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                List.of(Cs2Map.MIRAGE, Cs2Map.INFERNO)
        );

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validateThrowsWhenMapsContainDuplicates() {
        CreatePostRequest request = new CreatePostRequest(
                "content",
                PostType.TEXT,
                PostVisibility.PUBLIC,
                List.of(Cs2Map.MIRAGE, Cs2Map.MIRAGE)
        );

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Maps cannot contain duplicates.");
    }
}
