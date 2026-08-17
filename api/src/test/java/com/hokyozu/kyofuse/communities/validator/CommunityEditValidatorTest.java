package com.hokyozu.kyofuse.communities.validator;

import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityEditValidatorTest {

    @Mock
    private CommunityRepository communityRepository;

    @InjectMocks
    private CommunityEditValidator validator;

    @Test
    void validatePassesWhenSlugIsUnchanged() {
        Community community = community("kyofuse-cs2");
        UpdateCommunityRequest request = request("kyofuse-cs2");

        assertThatCode(() -> validator.validate(request, community)).doesNotThrowAnyException();

        verify(communityRepository, never()).existsBySlugAndIdNot(any(), any());
    }

    @Test
    void validatePassesWhenSlugIsNullInRequest() {
        Community community = community("kyofuse-cs2");
        UpdateCommunityRequest request = request(null);

        assertThatCode(() -> validator.validate(request, community)).doesNotThrowAnyException();

        verify(communityRepository, never()).existsBySlugAndIdNot(any(), any());
    }

    @Test
    void validatePassesWhenNewSlugIsAvailable() {
        Community community = community("kyofuse-cs2");
        UpdateCommunityRequest request = request("kyofuse-updated");
        when(communityRepository.existsBySlugAndIdNot("kyofuse-updated", community.getId())).thenReturn(false);

        assertThatCode(() -> validator.validate(request, community)).doesNotThrowAnyException();
    }

    @Test
    void validateThrowsConflictWhenNewSlugAlreadyInUse() {
        Community community = community("kyofuse-cs2");
        UpdateCommunityRequest request = request("kyofuse-updated");
        when(communityRepository.existsBySlugAndIdNot("kyofuse-updated", community.getId())).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(request, community))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Slug já está em uso.");
    }

    private UpdateCommunityRequest request(String slug) {
        return new UpdateCommunityRequest(null, slug, null, null, null, null);
    }

    private Community community(String slug) {
        return Community.builder()
                .id(UUID.randomUUID())
                .owner(User.builder().id(UUID.randomUUID()).username("owner").build())
                .name("Kyofuse CS2")
                .slug(slug)
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

}
