package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.validator.EmailAndUsernameAvailabilityValidator;
import com.hokyozu.kyofuse.auth.validator.LoginFinderValidator;
import com.hokyozu.kyofuse.auth.validator.LoginValidator;
import com.hokyozu.kyofuse.infrastructure.security.jwt.JwtService;
import com.hokyozu.kyofuse.profiles.service.GamerProfileService;
import com.hokyozu.kyofuse.relationships.privacy.service.UserPrivacySettingsService;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private GamerProfileService gamerProfileService;

    @Mock
    private UserPrivacySettingsService userPrivacySettingsService;

    @Mock
    private EmailAndUsernameAvailabilityValidator emailAndUsernameAvailabilityValidator;

    @Mock
    private LoginFinderValidator loginFinderValidator;

    @Mock
    private LoginValidator loginValidator;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserProfileAndToken() {
        RegisterRequest request = new RegisterRequest(
                " Hideo ",
                " Kojima ",
                " hideo@example.com ",
                " hideo ",
                "password123"
        );
        UUID userId = UUID.randomUUID();

        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(emailAndUsernameAvailabilityValidator).validate(" hideo@example.com ", " hideo ");
        verify(userRepository).save(userCaptor.capture());
        verify(gamerProfileService).createGamerProfileMin(userCaptor.getValue());
        verify(userPrivacySettingsService).createDefault(userCaptor.getValue());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getFirstName()).isEqualTo("Hideo");
        assertThat(savedUser.getLastName()).isEqualTo("Kojima");
        assertThat(savedUser.getEmail()).isEqualTo("hideo@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("hideo");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("hideo@example.com");
        assertThat(response.username()).isEqualTo("hideo");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    void loginValidatesUserAndReturnsToken() {
        LoginRequest request = new LoginRequest("hideo", "password123");
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("hideo@example.com")
                .username("hideo")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(loginFinderValidator.validate(request)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(loginValidator).validate(user, request);
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo(user.getEmail());
    }
}
