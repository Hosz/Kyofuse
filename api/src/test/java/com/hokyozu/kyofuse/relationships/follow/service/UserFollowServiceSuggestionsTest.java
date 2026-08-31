package com.hokyozu.kyofuse.relationships.follow.service;

import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.permission.service.follow.FollowPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceSuggestionsTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private UserChecker userChecker;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProfilePermissionService profilePermissionService;

    @Mock
    private UserBlockRepository userBlockRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private FollowPermissionService followPermissionService;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @Mock
    private GamerProfileRepository gamerProfileRepository;

    @Mock
    private GamerProfileFavoriteMapRepository gamerProfileFavoriteMapRepository;

    @InjectMocks
    private UserFollowService userFollowService;

    @Test
    void excludesCurrentUserFollowedUsersAndBlockedUsersFromSuggestions() {
        UUID userId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();
        UUID blockerId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        User user = User.builder().id(userId).username("me").build();
        User candidateUser = User.builder().id(candidateId).username("candidate").build();
        GamerProfile candidateProfile = GamerProfile.builder()
                .id(UUID.randomUUID())
                .user(candidateUser)
                .nickname("CandidateNick")
                .avatarUrl("avatar.png")
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFollowRepository.findFollowedIdsByFollower(user)).thenReturn(List.of(followedId));
        when(userBlockRepository.findBlockedIdsByBlocker(user)).thenReturn(List.of(blockedId));
        when(userBlockRepository.findBlockerIdsByBlocked(user)).thenReturn(List.of(blockerId));

        when(gamerProfileRepository.findSuggestions(eq(UserStatus.ACTIVE), any(Collection.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(candidateProfile), pageable, 1));
        when(gamerProfileFavoriteMapRepository.findByProfile_IdIn(any())).thenReturn(List.of());

        Page<GamerProfileResponse> result = userFollowService.getFollowSuggestions(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().username()).isEqualTo("candidate");
        assertThat(result.getContent().getFirst().nickname()).isEqualTo("CandidateNick");

        verify(userChecker).checkActive(user);
    }
}
