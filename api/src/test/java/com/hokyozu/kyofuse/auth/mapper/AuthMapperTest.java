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

    private final AuthMapper authMapper = new AuthMapper();

    @Test
    void toAuthResponse_shouldMapUserToAuthResponse_successfully() {
        User user = createUser();
        String token = "jwt.token.here";

        AuthResponse response = authMapper.toAuthResponse(user, token);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getRole()).isEqualTo(user.getRole());
        assertThat(response.getToken()).isEqualTo(token);
    }

    @Test
    void toAuthResponse_shouldContainAllUserFields() {
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String email = "test@example.com";
        UserRole role = UserRole.USER;

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        String token = "valid.jwt.token";

        AuthResponse response = authMapper.toAuthResponse(user, token);

        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getUsername()).isEqualTo(username);
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.getRole()).isEqualTo(role);
        assertThat(response.getToken()).isEqualTo(token);
    }

    @Test
    void toAuthResponse_shouldThrowException_whenUserIsNull() {
        String token = "valid.token";

        assertThatThrownBy(() -> authMapper.toAuthResponse(null, token))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toAuthResponse_shouldThrowException_whenTokenIsNull() {
        User user = createUser();

        assertThatThrownBy(() -> authMapper.toAuthResponse(user, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toAuthResponse_shouldMapAdminRole() {
        User user = createUser();
        user.setRole(UserRole.ADMIN);
        String token = "admin.token";

        AuthResponse response = authMapper.toAuthResponse(user, token);

        assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void toAuthResponse_shouldMapDifferentTokens() {
        User user = createUser();
        String token1 = "token.1";
        String token2 = "token.2";

        AuthResponse response1 = authMapper.toAuthResponse(user, token1);
        AuthResponse response2 = authMapper.toAuthResponse(user, token2);

        assertThat(response1.getToken()).isEqualTo(token1);
        assertThat(response2.getToken()).isEqualTo(token2);
        assertThat(response1.getToken()).isNotEqualTo(response2.getToken());
    }

    @Test
    void toAuthResponse_shouldMapEmptyStringToken() {
        User user = createUser();
        String emptyToken = "";

        AuthResponse response = authMapper.toAuthResponse(user, emptyToken);

        assertThat(response.getToken()).isEqualTo(emptyToken);
    }

    @Test
    void toAuthResponse_shouldNotMapPassword() {
        User user = createUser();
        user.setPassword("secret_password_123");
        String token = "jwt.token";

        AuthResponse response = authMapper.toAuthResponse(user, token);

        assertThat(response).isNotNull();
        try {
            response.getClass().getDeclaredField("password");
            if (response.getClass().getDeclaredMethod("getPassword", new Class[0]) != null) {
                throw new AssertionError("Password field should not be exposed");
            }
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            // Expected: password should not be in response
        }
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
