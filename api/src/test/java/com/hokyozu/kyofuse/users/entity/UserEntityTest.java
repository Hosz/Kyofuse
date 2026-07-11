package com.hokyozu.kyofuse.users.entity;

import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void shouldCreateUserWithAllFields() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("hashed_password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("hashed_password");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateUserPassword() {
        User user = createUser();
        String newPassword = "new_hashed_password";

        user.setPassword(newPassword);

        assertThat(user.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void shouldUpdateUserStatus() {
        User user = createUser();

        user.setStatus(UserStatus.INACTIVE);

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void shouldUpdateUserRole() {
        User user = createUser();

        user.setRole(UserRole.ADMIN);

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldHandleDifferentUserRoles() {
        User user = createUser();

        for (UserRole role : UserRole.values()) {
            user.setRole(role);
            assertThat(user.getRole()).isEqualTo(role);
        }
    }

    @Test
    void shouldHandleDifferentUserStatuses() {
        User user = createUser();

        for (UserStatus status : UserStatus.values()) {
            user.setStatus(status);
            assertThat(user.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void shouldUpdateTimestamps() {
        User user = createUser();
        Instant originalCreatedAt = user.getCreatedAt();
        Instant newUpdatedAt = Instant.now().plusSeconds(3600);

        user.setUpdatedAt(newUpdatedAt);

        assertThat(user.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(user.getUpdatedAt()).isEqualTo(newUpdatedAt);
    }

    @Test
    void shouldUpdateEmail() {
        User user = createUser();
        String newEmail = "newemail@example.com";

        user.setEmail(newEmail);

        assertThat(user.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void shouldUpdateUsername() {
        User user = createUser();
        String newUsername = "newusername";

        user.setUsername(newUsername);

        assertThat(user.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void shouldHaveDefaultValues() {
        User user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    void shouldAllowNullPassword() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword(null);

        assertThat(user.getPassword()).isNull();
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("hashed_password");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }
}
