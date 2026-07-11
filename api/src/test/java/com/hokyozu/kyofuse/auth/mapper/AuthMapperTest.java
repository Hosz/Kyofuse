package com.hokyozu.kyofuse.auth.mapper;

import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
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
        String token = "jwt.token.here";

        AuthResponse response = AuthMapper.toResponse(user, token);

        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
        assertThat(response.email()).isEqualTo(user.getEmail());
        assertThat(response.role()).isEqualTo(user.getRole().name());
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.tokenType()).isEqualTo("Bearer");
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

        String token = "valid.jwt.token";

        AuthResponse response = AuthMapper.toResponse(user, token);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo(username);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.role()).isEqualTo(role.name());
        assertThat(response.token()).isEqualTo(token);
    }

    @Test
    void toResponse_shouldThrowException_whenUserIsNull() {
        String token = "valid.token";

        assertThatThrownBy(() -> AuthMapper.toResponse(null, token))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toResponse_shouldThrowException_whenTokenIsNull() {
        User user = createUser();

        assertThatThrownBy(() -> AuthMapper.toResponse(user, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toResponse_shouldMapAdminRole() {
        User user = createUser();
        user.setRole(UserRole.ADMIN);
        String token = "admin.token";

        AuthResponse response = AuthMapper.toResponse(user, token);

        assertThat(response.role()).isEqualTo(UserRole.ADMIN.name());
    }

    @Test
    void toResponse_shouldMapDifferentTokens() {
        User user = createUser();
        String token1 = "token.1";
        String token2 = "token.2";

        AuthResponse response1 = AuthMapper.toResponse(user, token1);
        AuthResponse response2 = AuthMapper.toResponse(user, token2);

        assertThat(response1.token()).isEqualTo(token1);
        assertThat(response2.token()).isEqualTo(token2);
        assertThat(response1.token()).isNotEqualTo(response2.token());
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
