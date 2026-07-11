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
        String email = "test@example.com";
        String username = "testuser";
        String firstName = "Test";
        String lastName = "User";
        Instant now = Instant.now();

        User user = User.builder()
                .id(userId)
                .email(email)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getFirstName()).isEqualTo(firstName);
        assertThat(user.getLastName()).isEqualTo(lastName);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldUpdateUserStatus() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        user.setStatus(UserStatus.INACTIVE);

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void shouldUpdateUserRole() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        user.setRole(UserRole.ADMIN);

        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldSetDeactivationTimestamp() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.INACTIVE)
                .build();

        Instant deactivatedAt = Instant.now();
        user.setDeactivatedAt(deactivatedAt);

        assertThat(user.getDeactivatedAt()).isEqualTo(deactivatedAt);
    }

    @Test
    void shouldSetDeletionScheduledTimestamp() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.BANNED)
                .build();

        Instant deletionScheduledAt = Instant.now();
        user.setDeletionScheduledAt(deletionScheduledAt);

        assertThat(user.getDeletionScheduledAt()).isEqualTo(deletionScheduledAt);
    }

    @Test
    void shouldBanUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        user.setStatus(UserStatus.BANNED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    void shouldUpdateUserEmail() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("old@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        user.setEmail("new@example.com");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldPreserveCreatedAtTimestamp() {
        Instant createdAt = Instant.now().minusSeconds(3600);
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(createdAt)
                .build();

        assertThat(user.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldAllowUpdatingLastName() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        user.setLastName("UpdatedLastName");

        assertThat(user.getLastName()).isEqualTo("UpdatedLastName");
    }

    @Test
    void shouldAllowUpdatingFirstName() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        user.setFirstName("UpdatedFirstName");

        assertThat(user.getFirstName()).isEqualTo("UpdatedFirstName");
    }
}
