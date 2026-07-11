package com.hokyozu.kyofuse.users.service;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCheckerTest {

    private UserChecker userChecker;

    @BeforeEach
    void setUp() {
        userChecker = new UserChecker();
    }

    @Test
    void checkActive_shouldNotThrowException_whenUserIsActive() {
        User activeUser = createUser(UserStatus.ACTIVE);

        assertThatNoException()
                .isThrownBy(() -> userChecker.checkActive(activeUser));
    }

    @Test
    void checkActive_shouldThrowException_whenUserIsInactive() {
        User inactiveUser = createUser(UserStatus.INACTIVE);

        assertThatThrownBy(() -> userChecker.checkActive(inactiveUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não ativo.");
    }

    @Test
    void checkActive_shouldThrowException_whenUserIsBanned() {
        User bannedUser = createUser(UserStatus.BANNED);

        assertThatThrownBy(() -> userChecker.checkActive(bannedUser))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não ativo.");
    }

    @Test
    void checkActive_shouldOnlyAcceptActiveStatus() {
        for (UserStatus status : UserStatus.values()) {
            User user = createUser(status);

            if (status == UserStatus.ACTIVE) {
                assertThatNoException()
                        .isThrownBy(() -> userChecker.checkActive(user));
            } else {
                assertThatThrownBy(() -> userChecker.checkActive(user))
                        .isInstanceOf(BadRequestException.class);
            }
        }
    }

    private User createUser(UserStatus status) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(status);
        return user;
    }
}
