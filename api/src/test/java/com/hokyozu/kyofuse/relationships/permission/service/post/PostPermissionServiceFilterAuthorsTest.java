package com.hokyozu.kyofuse.relationships.permission.service.post;

import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * filterViewableAuthors decide, em lote, o mesmo que canViewAuthorPosts decidiria um a
 * um — mas com um número fixo de consultas. Estes testes travam as duas coisas: o
 * resultado e a quantidade de idas ao banco.
 */
@ExtendWith(MockitoExtension.class)
class PostPermissionServiceFilterAuthorsTest {

    @Mock
    private BlockValidator blockValidator;

    @Mock
    private UserPrivacySettingsRepository userPrivacySettingsRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private UserFriendshipRepository userFriendshipRepository;

    @InjectMocks
    private PostPermissionService postPermissionService;

    @Test
    void returnsEmptyWithoutTouchingTheDatabaseWhenThereAreNoAuthors() {
        User viewer = user();

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(userPrivacySettingsRepository, userFollowRepository, userFriendshipRepository);
    }

    @Test
    void keepsPublicAuthorsWithoutAskingAboutRelationships() {
        User viewer = user();
        User first = user();
        User second = user();
        when(userPrivacySettingsRepository.findAllByUserIn(List.of(first, second)))
                .thenReturn(List.of(settings(first, ProfileVisibility.PUBLIC), settings(second, ProfileVisibility.PUBLIC)));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of(first, second));

        assertThat(result).containsExactly(first, second);
        verify(userFollowRepository, never()).findFollowedIdsByFollowerAndFollowedIn(any(), anyList());
        verify(userFriendshipRepository, never()).findMutualFriendIdsIn(any(), anyList());
    }

    @Test
    void dropsPrivateAuthors() {
        User viewer = user();
        User author = user();
        when(userPrivacySettingsRepository.findAllByUserIn(List.of(author)))
                .thenReturn(List.of(settings(author, ProfileVisibility.PRIVATE)));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of(author));

        assertThat(result).isEmpty();
    }

    @Test
    void keepsFollowersOnlyAuthorsOnlyWhenTheViewerFollowsThem() {
        User viewer = user();
        User followed = user();
        User notFollowed = user();
        when(userPrivacySettingsRepository.findAllByUserIn(List.of(followed, notFollowed)))
                .thenReturn(List.of(
                        settings(followed, ProfileVisibility.FOLLOWERS),
                        settings(notFollowed, ProfileVisibility.FOLLOWERS)
                ));
        when(userFollowRepository.findFollowedIdsByFollowerAndFollowedIn(viewer, List.of(followed, notFollowed)))
                .thenReturn(List.of(followed.getId()));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of(followed, notFollowed));

        assertThat(result).containsExactly(followed);
    }

    @Test
    void keepsFriendsOnlyAuthorsOnlyWhenThereIsMutualFriendship() {
        User viewer = user();
        User friend = user();
        User stranger = user();
        when(userPrivacySettingsRepository.findAllByUserIn(List.of(friend, stranger)))
                .thenReturn(List.of(
                        settings(friend, ProfileVisibility.FRIENDS),
                        settings(stranger, ProfileVisibility.FRIENDS)
                ));
        when(userFriendshipRepository.findMutualFriendIdsIn(viewer, List.of(friend, stranger)))
                .thenReturn(List.of(friend.getId()));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of(friend, stranger));

        assertThat(result).containsExactly(friend);
    }

    @Test
    void asksAboutFollowersAndFriendsOnceEachRegardlessOfHowManyAuthors() {
        User viewer = user();
        User publicAuthor = user();
        User followerAuthor = user();
        User otherFollowerAuthor = user();
        User friendAuthor = user();
        User privateAuthor = user();
        List<User> authors = List.of(publicAuthor, followerAuthor, otherFollowerAuthor, friendAuthor, privateAuthor);
        when(userPrivacySettingsRepository.findAllByUserIn(authors)).thenReturn(List.of(
                settings(publicAuthor, ProfileVisibility.PUBLIC),
                settings(followerAuthor, ProfileVisibility.FOLLOWERS),
                settings(otherFollowerAuthor, ProfileVisibility.FOLLOWERS),
                settings(friendAuthor, ProfileVisibility.FRIENDS),
                settings(privateAuthor, ProfileVisibility.PRIVATE)
        ));
        when(userFollowRepository.findFollowedIdsByFollowerAndFollowedIn(
                viewer, List.of(followerAuthor, otherFollowerAuthor)
        )).thenReturn(List.of(followerAuthor.getId()));
        when(userFriendshipRepository.findMutualFriendIdsIn(viewer, List.of(friendAuthor)))
                .thenReturn(List.of(friendAuthor.getId()));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, authors);

        assertThat(result).containsExactly(publicAuthor, followerAuthor, friendAuthor);
        verify(userPrivacySettingsRepository).findAllByUserIn(authors);
        verify(userFollowRepository).findFollowedIdsByFollowerAndFollowedIn(any(), anyList());
        verify(userFriendshipRepository).findMutualFriendIdsIn(any(), anyList());
    }

    @Test
    void preservesTheOrderOfTheGivenAuthors() {
        User viewer = user();
        User first = user();
        User second = user();
        User third = user();
        when(userPrivacySettingsRepository.findAllByUserIn(List.of(first, second, third))).thenReturn(List.of(
                settings(third, ProfileVisibility.PUBLIC),
                settings(first, ProfileVisibility.PUBLIC),
                settings(second, ProfileVisibility.PUBLIC)
        ));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of(first, second, third));

        assertThat(result).containsExactly(first, second, third);
    }

    @Test
    void keepsTheViewerEvenWhenTheirOwnPostsAreRestricted() {
        // Mesma saída de canViewAuthorPosts: o próprio usuário sempre vê os posts dele.
        User viewer = user();
        when(userPrivacySettingsRepository.findAllByUserIn(List.of(viewer)))
                .thenReturn(List.of(settings(viewer, ProfileVisibility.PRIVATE)));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of(viewer));

        assertThat(result).containsExactly(viewer);
    }

    @Test
    void dropsAuthorsWithoutPrivacySettingsInsteadOfExposingThem() {
        User viewer = user();
        User withSettings = user();
        User withoutSettings = user();
        when(userPrivacySettingsRepository.findAllByUserIn(List.of(withSettings, withoutSettings)))
                .thenReturn(List.of(settings(withSettings, ProfileVisibility.PUBLIC)));

        List<User> result = postPermissionService.filterViewableAuthors(viewer, List.of(withSettings, withoutSettings));

        assertThat(result).containsExactly(withSettings);
    }

    private static User user() {
        return User.builder().id(UUID.randomUUID()).username("player").build();
    }

    private static UserPrivacySettings settings(User user, ProfileVisibility postsVisibility) {
        return UserPrivacySettings.builder()
                .id(user.getId())
                .user(user)
                .postsVisibility(postsVisibility)
                .build();
    }
}
