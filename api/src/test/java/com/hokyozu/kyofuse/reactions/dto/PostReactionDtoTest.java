package com.hokyozu.kyofuse.reactions.dto;

import com.hokyozu.kyofuse.reactions.dto.request.PostReactionRequest;
import com.hokyozu.kyofuse.reactions.dto.response.PostReactionResponse;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostReactionDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void requestAcceptsReactionAndRejectsNull() {
        assertThat(validator.validate(new PostReactionRequest(ReactionType.CLUTCH))).isEmpty();
        assertThat(validator.validate(new PostReactionRequest(null)))
                .singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("reactionType");
                    assertThat(violation.getMessage()).isEqualTo("É necessária uma reação");
                });
    }

    @Test
    void responseExposesValues() {
        UUID postId = UUID.randomUUID();
        PostReactionResponse response = new PostReactionResponse(postId, "player", ReactionType.LOL);

        assertThat(response.postId()).isEqualTo(postId);
        assertThat(response.username()).isEqualTo("player");
        assertThat(response.reactionType()).isEqualTo(ReactionType.LOL);
    }
}
