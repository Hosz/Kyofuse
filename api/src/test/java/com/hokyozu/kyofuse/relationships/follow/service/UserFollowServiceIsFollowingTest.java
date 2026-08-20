package com.hokyozu.kyofuse.relationships.follow.service;

import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.permission.service.follow.FollowPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * isFollowing existe para o botão de seguir do front nascer com o estado certo. Antes
 * dele o botão dizia "Seguir" mesmo para quem o usuário já seguia, e o clique batia num
 * follow duplicado que o backend recusa.
 */
@ExtendWith(MockitoExtension.class)
class UserFollowServiceIsFollowingTest {

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

    @InjectMocks
    private UserFollowService userFollowService;

    @Test
    void returnsTrueWhenTheFollowAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User other = User.builder().id(otherId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFinder.findProfileByUserId(otherId)).thenReturn(other);
        when(userFollowRepository.existsByFollowerAndFollowed(user, other)).thenReturn(true);

        assertThat(userFollowService.isFollowing(userId, otherId)).isTrue();
    }

    @Test
    void returnsFalseWhenThereIsNoFollow() {
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User other = User.builder().id(otherId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFinder.findProfileByUserId(otherId)).thenReturn(other);
        when(userFollowRepository.existsByFollowerAndFollowed(user, other)).thenReturn(false);

        assertThat(userFollowService.isFollowing(userId, otherId)).isFalse();
    }

    @Test
    void asksInTheRightDirection() {
        // existsByFollowerAndFollowed não é simétrico: o seguidor é quem pergunta.
        UUID userId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        User other = User.builder().id(otherId).build();
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userFinder.findProfileByUserId(otherId)).thenReturn(other);
        when(userFollowRepository.existsByFollowerAndFollowed(user, other)).thenReturn(true);

        userFollowService.isFollowing(userId, otherId);

        verify(userFollowRepository).existsByFollowerAndFollowed(user, other);
    }
}
