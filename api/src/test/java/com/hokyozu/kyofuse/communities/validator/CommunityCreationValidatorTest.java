package com.hokyozu.kyofuse.communities.validator;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityCreationValidatorTest {

    @Mock
    private CommunityRepository communityRepository;

    @InjectMocks
    private CommunityCreationValidator validator;

    @Test
    void validatePassesWhenSlugIsAvailable() {
        when(communityRepository.existsBySlug("kyofuse-cs2")).thenReturn(false);

        assertThatCode(() -> validator.validate(request("kyofuse-cs2")))
                .doesNotThrowAnyException();
    }

    @Test
    void validateThrowsConflictWhenSlugAlreadyInUse() {
        when(communityRepository.existsBySlug("kyofuse-cs2")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(request("kyofuse-cs2")))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Slug já está em uso.");
    }

    private CommunityRequest request(String slug) {
        return new CommunityRequest(
                "Kyofuse CS2",
                slug,
                "Community description",
                null,
                null,
                CommunityVisibility.PUBLIC
        );
    }
}
