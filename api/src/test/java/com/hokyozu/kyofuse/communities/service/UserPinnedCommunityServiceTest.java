package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.UserPinnedCommunity;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.repository.UserPinnedCommunityRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPinnedCommunityServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private UserPinnedCommunityRepository userPinnedCommunityRepository;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private UserPinnedCommunityService userPinnedCommunityService;

    @Test
    void listPinnedCommunitiesReturnsPinnedCommunitiesInPinOrder() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community first = community(CommunityStatus.ACTIVE);
        Community second = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId))
                .thenReturn(List.of(pinned(user, first), pinned(user, second)));

        List<CommunityResponse> response = userPinnedCommunityService.listPinnedCommunities(userId);

        assertThat(response).extracting(CommunityResponse::id).containsExactly(first.getId(), second.getId());
    }

    @Test
    void listPinnedCommunitiesSkipsArchivedCommunities() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community active = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId))
                .thenReturn(List.of(pinned(user, community(CommunityStatus.ARCHIVED)), pinned(user, active)));

        List<CommunityResponse> response = userPinnedCommunityService.listPinnedCommunities(userId);

        assertThat(response).extracting(CommunityResponse::id).containsExactly(active.getId());
    }

    @Test
    void pinCommunitySavesPinForActiveCommunity() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community community = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(userPinnedCommunityRepository.existsByUserIdAndCommunityId(userId, community.getId())).thenReturn(false);

        CommunityResponse response = userPinnedCommunityService.pinCommunity(userId, community.getId());

        ArgumentCaptor<UserPinnedCommunity> captor = ArgumentCaptor.forClass(UserPinnedCommunity.class);
        verify(userPinnedCommunityRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(captor.getValue().getCommunity()).isSameAs(community);
        assertThat(response.id()).isEqualTo(community.getId());
    }

    @Test
    void pinCommunityRejectsDuplicatePin() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community community = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(userPinnedCommunityRepository.existsByUserIdAndCommunityId(userId, community.getId())).thenReturn(true);

        assertThatThrownBy(() -> userPinnedCommunityService.pinCommunity(userId, community.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Community is already pinned");

        verify(userPinnedCommunityRepository, never()).save(any());
    }

    @Test
    void pinCommunityRejectsArchivedCommunity() {
        UUID userId = UUID.randomUUID();
        Community community = community(CommunityStatus.ARCHIVED);

        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId));
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));

        assertThatThrownBy(() -> userPinnedCommunityService.pinCommunity(userId, community.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community not found");
    }

    @Test
    void pinCommunityRejectsMissingCommunity() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId));
        when(communityRepository.findById(communityId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPinnedCommunityService.pinCommunity(userId, communityId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void unpinCommunityDeletesExistingPin() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community community = community(CommunityStatus.ACTIVE);
        UserPinnedCommunity pin = pinned(user, community);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userPinnedCommunityRepository.findByUserIdAndCommunityId(userId, community.getId()))
                .thenReturn(Optional.of(pin));

        userPinnedCommunityService.unpinCommunity(userId, community.getId());

        verify(userPinnedCommunityRepository).delete(pin);
    }

    @Test
    void unpinCommunityRejectsWhenNotPinned() {
        UUID userId = UUID.randomUUID();
        UUID communityId = UUID.randomUUID();

        when(userFinder.findProfileByUserId(userId)).thenReturn(activeUser(userId));
        when(userPinnedCommunityRepository.findByUserIdAndCommunityId(userId, communityId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPinnedCommunityService.unpinCommunity(userId, communityId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Community is not pinned");
    }

    @Test
    void pinCommunityPutsNewPinAtTheEndOfTheBar() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community community = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(communityRepository.findById(community.getId())).thenReturn(Optional.of(community));
        when(userPinnedCommunityRepository.existsByUserIdAndCommunityId(userId, community.getId())).thenReturn(false);
        when(userPinnedCommunityRepository.countByUserId(userId)).thenReturn(2);

        userPinnedCommunityService.pinCommunity(userId, community.getId());

        ArgumentCaptor<UserPinnedCommunity> captor = ArgumentCaptor.forClass(UserPinnedCommunity.class);
        verify(userPinnedCommunityRepository).save(captor.capture());
        assertThat(captor.getValue().getPosition()).isEqualTo(2);
    }

    @Test
    void reorderPinnedCommunitiesAppliesTheGivenOrder() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community first = community(CommunityStatus.ACTIVE);
        Community second = community(CommunityStatus.ACTIVE);
        Community third = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId))
                .thenReturn(List.of(pinned(user, first, 0), pinned(user, second, 1), pinned(user, third, 2)));

        List<CommunityResponse> response = userPinnedCommunityService.reorderPinnedCommunities(
                userId, List.of(third.getId(), first.getId(), second.getId()));

        assertThat(response).extracting(CommunityResponse::id)
                .containsExactly(third.getId(), first.getId(), second.getId());

        ArgumentCaptor<List<UserPinnedCommunity>> captor = ArgumentCaptor.forClass(List.class);
        verify(userPinnedCommunityRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(UserPinnedCommunity::getPosition).containsExactly(0, 1, 2);
        assertThat(captor.getValue()).extracting(item -> item.getCommunity().getId())
                .containsExactly(third.getId(), first.getId(), second.getId());
    }

    @Test
    void reorderPinnedCommunitiesRejectsListThatDoesNotMatchWhatIsPinned() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community first = community(CommunityStatus.ACTIVE);
        Community second = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId))
                .thenReturn(List.of(pinned(user, first, 0), pinned(user, second, 1)));

        // Lista incompleta: outra aba pode ter fixado algo no meio tempo.
        assertThatThrownBy(() -> userPinnedCommunityService.reorderPinnedCommunities(userId, List.of(first.getId())))
                .isInstanceOf(BadRequestException.class);

        verify(userPinnedCommunityRepository, never()).saveAll(any());
    }

    @Test
    void reorderPinnedCommunitiesRejectsRepeatedIds() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community first = community(CommunityStatus.ACTIVE);
        Community second = community(CommunityStatus.ACTIVE);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId))
                .thenReturn(List.of(pinned(user, first, 0), pinned(user, second, 1)));

        assertThatThrownBy(() -> userPinnedCommunityService.reorderPinnedCommunities(
                userId, List.of(first.getId(), first.getId())))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void unpinCommunityReindexesRemainingPins() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId);
        Community removed = community(CommunityStatus.ACTIVE);
        Community kept = community(CommunityStatus.ACTIVE);
        UserPinnedCommunity pin = pinned(user, removed, 0);

        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userPinnedCommunityRepository.findByUserIdAndCommunityId(userId, removed.getId()))
                .thenReturn(Optional.of(pin));
        // Depois do delete resta só a outra, ainda com a posição antiga.
        when(userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId))
                .thenReturn(List.of(pinned(user, kept, 1)));

        userPinnedCommunityService.unpinCommunity(userId, removed.getId());

        ArgumentCaptor<List<UserPinnedCommunity>> captor = ArgumentCaptor.forClass(List.class);
        verify(userPinnedCommunityRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(UserPinnedCommunity::getPosition).containsExactly(0);
    }

    private User activeUser(UUID userId) {
        return User.builder().id(userId).username("player").status(UserStatus.ACTIVE).build();
    }

    private Community community(CommunityStatus status) {
        return Community.builder()
                .id(UUID.randomUUID())
                .owner(User.builder().id(UUID.randomUUID()).username("owner").build())
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2-" + UUID.randomUUID())
                .visibility(CommunityVisibility.PUBLIC)
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private UserPinnedCommunity pinned(User user, Community community) {
        return pinned(user, community, 0);
    }

    private UserPinnedCommunity pinned(User user, Community community, int position) {
        return UserPinnedCommunity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .community(community)
                .position(position)
                .createdAt(Instant.now())
                .build();
    }
}
