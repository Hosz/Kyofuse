package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.dto.response.RegisterResponse;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthMapperTest {

    @Test
    void toResponse_shouldMapUserToAuthResponse_successfully() {
        User user = createUser();

        AuthResponse response = AuthMapper.toResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.role()).isEqualTo(user.getRole().name());
    }

    @Test
    void toRegisterResponse_shouldMapUserToRegisterResponse() {
        User user = createUser();
        user.setEmailVerified(false);

        RegisterResponse response = AuthMapper.toRegisterResponse(user);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.emailVerified()).isFalse();
    }

    @Test
    void toResponse_shouldContainAllUserFields() {
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String email = "test@example.com";
        UserRole role = UserRole.USER;

        User user = User.builder()
                .id(userId)
                .username(username)
                .email(email)
                .role(role)
                .status(UserStatus.ACTIVE)
                .firstName("Test")
                .lastName("User")
                .build();

        AuthResponse response = AuthMapper.toResponse(user);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo(username);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.role()).isEqualTo(role.name());
    }

    @Test
    void toResponse_shouldThrowException_whenUserIsNull() {
        assertThatThrownBy(() -> AuthMapper.toResponse(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toResponse_shouldMapAdminRole() {
        User user = createUser();
        user.setRole(UserRole.ADMIN);

        AuthResponse response = AuthMapper.toResponse(user);

        assertThat(response.role()).isEqualTo(UserRole.ADMIN.name());
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
