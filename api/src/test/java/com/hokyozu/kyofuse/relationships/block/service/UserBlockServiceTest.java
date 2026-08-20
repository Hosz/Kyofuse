package com.hokyozu.kyofuse.relationships.block.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.relationships.block.dto.response.UserBlockResponse;
import com.hokyozu.kyofuse.relationships.block.entity.UserBlock;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserBlockServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private GamerProfileFinder gamerProfileFinder;

    @Mock
    private UserBlockRepository userBlockRepository;

    @Spy
    private UserChecker userChecker = new UserChecker();

    @InjectMocks
    private UserBlockService userBlockService;

    @Test
    void blockUserSavesBlockBetweenTwoActiveUsers() {
        User blocker = activeUser("blocker");
        User blocked = activeUser("blocked");

        when(userFinder.findProfileByUserId(blocker.getId())).thenReturn(blocker);
        when(userFinder.findProfileByUserId(blocked.getId())).thenReturn(blocked);
        when(userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(false);
        when(userBlockRepository.existsByBlockedAndBlocker(blocker, blocked)).thenReturn(false);

        UserBlockResponse response = userBlockService.blockUser(blocker.getId(), blocked.getId());

        ArgumentCaptor<UserBlock> captor = ArgumentCaptor.forClass(UserBlock.class);
        verify(userBlockRepository).save(captor.capture());
        assertThat(captor.getValue().getBlocker()).isSameAs(blocker);
        assertThat(captor.getValue().getBlocked()).isSameAs(blocked);
        assertThat(response.blockedId()).isEqualTo(blocked.getId());
    }

    @Test
    void blockUserRejectsBlockingYourself() {
        User user = activeUser("player");
        when(userFinder.findProfileByUserId(user.getId())).thenReturn(user);

        assertThatThrownBy(() -> userBlockService.blockUser(user.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("You cannot block yourself.");

        verify(userBlockRepository, never()).save(any());
    }

    @Test
    void blockUserRejectsAlreadyBlockedUser() {
        User blocker = activeUser("blocker");
        User blocked = activeUser("blocked");

        when(userFinder.findProfileByUserId(blocker.getId())).thenReturn(blocker);
        when(userFinder.findProfileByUserId(blocked.getId())).thenReturn(blocked);
        when(userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(true);

        assertThatThrownBy(() -> userBlockService.blockUser(blocker.getId(), blocked.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is already blocked.");

        verify(userBlockRepository, never()).save(any());
    }

    @Test
    void blockUserRejectsWhenTheOtherSideAlreadyBlockedYou() {
        User blocker = activeUser("blocker");
        User blocked = activeUser("blocked");

        when(userFinder.findProfileByUserId(blocker.getId())).thenReturn(blocker);
        when(userFinder.findProfileByUserId(blocked.getId())).thenReturn(blocked);
        when(userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(false);
        when(userBlockRepository.existsByBlockedAndBlocker(blocker, blocked)).thenReturn(true);

        assertThatThrownBy(() -> userBlockService.blockUser(blocker.getId(), blocked.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is already blocking you.");
    }

    @Test
    void getBlockedUsersEnrichesEachEntryWithNicknameAndAvatar() {
        User blocker = activeUser("blocker");
        User blocked = activeUser("blocked");
        UserBlock block = block(blocker, blocked);
        Pageable pageable = PageRequest.of(0, 20);

        when(userFinder.findProfileByUserId(blocker.getId())).thenReturn(blocker);
        when(userBlockRepository.findAllByBlocker(blocker, pageable))
                .thenReturn(new PageImpl<>(List.of(block), pageable, 1));
        when(gamerProfileFinder.findProfileByUserId(blocked.getId()))
                .thenReturn(GamerProfile.builder().nickname("Blocked One").avatarUrl("https://example.com/a.png").build());

        Page<UserBlockResponse> response = userBlockService.getBlockedUsers(blocker.getId(), pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().blockedUsername()).isEqualTo("blocked");
        assertThat(response.getContent().getFirst().blockedNickname()).isEqualTo("Blocked One");
        assertThat(response.getContent().getFirst().blockedAvatarUrl()).isEqualTo("https://example.com/a.png");
    }

    @Test
    void unblockUserDeletesExistingBlock() {
        User blocker = activeUser("blocker");
        User blocked = activeUser("blocked");
        UserBlock block = block(blocker, blocked);

        when(userFinder.findProfileByUserId(blocker.getId())).thenReturn(blocker);
        when(userFinder.findProfileByUserId(blocked.getId())).thenReturn(blocked);
        when(userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(true);
        when(userBlockRepository.findByBlockerAndBlocked(blocker, blocked)).thenReturn(block);

        userBlockService.unblockUser(blocker.getId(), blocked.getId());

        verify(userBlockRepository).delete(block);
    }

    @Test
    void unblockUserRejectsWhenUserIsNotBlocked() {
        User blocker = activeUser("blocker");
        User blocked = activeUser("blocked");

        when(userFinder.findProfileByUserId(blocker.getId())).thenReturn(blocker);
        when(userFinder.findProfileByUserId(blocked.getId())).thenReturn(blocked);
        when(userBlockRepository.existsByBlockerAndBlocked(blocker, blocked)).thenReturn(false);

        assertThatThrownBy(() -> userBlockService.unblockUser(blocker.getId(), blocked.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is not blocked.");

        verify(userBlockRepository, never()).delete(any());
    }

    private User activeUser(String username) {
        return User.builder().id(UUID.randomUUID()).username(username).status(UserStatus.ACTIVE).build();
    }

    private UserBlock block(User blocker, User blocked) {
        return UserBlock.builder()
                .id(UUID.randomUUID())
                .blocker(blocker)
                .blocked(blocked)
                .createdAt(Instant.now())
                .build();
    }
}
