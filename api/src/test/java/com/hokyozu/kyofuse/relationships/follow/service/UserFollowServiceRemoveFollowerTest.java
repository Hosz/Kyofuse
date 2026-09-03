package com.hokyozu.kyofuse.relationships.follow.service;

import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.follow.entity.UserFollow;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.permission.service.follow.FollowPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFollowServiceRemoveFollowerTest {

    @Mock
    private UserFinder userFinder;

    @Spy
    private UserChecker userChecker = new UserChecker();

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

    @InjectMocks
    private UserFollowService userFollowService;

    private User owner;
    private User follower;
    private UUID ownerId;
    private UUID followerId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        followerId = UUID.randomUUID();

        owner = User.builder().id(ownerId).username("owner").status(UserStatus.ACTIVE).build();
        follower = User.builder().id(followerId).username("follower").status(UserStatus.ACTIVE).build();

        when(userFinder.findProfileByUserId(ownerId)).thenReturn(owner);
        when(userFinder.findProfileByUserId(followerId)).thenReturn(follower);
    }

    @Test
    @DisplayName("Deve deletar vínculo na direção correta: follower é seguidor e owner é seguido")
    void removeFollowerDeletesInCorrectDirection() {
        UserFollow userFollow = UserFollow.builder()
                .follower(follower)
                .followed(owner)
                .build();

        when(userFollowRepository.findByFollowerAndFollowed(follower, owner)).thenReturn(userFollow);

        userFollowService.removeFollower(ownerId, followerId);

        verify(followPermissionService).validateRemoveFollower(owner, follower);
        verify(userFollowRepository).findByFollowerAndFollowed(follower, owner);
        verify(userFollowRepository, never()).findByFollowerAndFollowed(owner, follower);
        verify(userFollowRepository).delete(userFollow);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando o vínculo de seguidor não for encontrado no banco")
    void removeFollowerThrowsNotFoundWhenFollowIsNull() {
        when(userFollowRepository.findByFollowerAndFollowed(follower, owner)).thenReturn(null);

        assertThatThrownBy(() -> userFollowService.removeFollower(ownerId, followerId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Follow relationship not found");

        verify(userFollowRepository, never()).delete(any());
    }
}
