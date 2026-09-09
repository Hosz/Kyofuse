package com.hokyozu.kyofuse.users.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.service.EmailVerificationService;
import com.hokyozu.kyofuse.infrastructure.security.oauth.GoogleTokenVerifierService;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.dto.request.ChangePasswordRequest;
import com.hokyozu.kyofuse.users.dto.request.UpdateEmailRequest;
import com.hokyozu.kyofuse.users.dto.request.UpdateUsernameRequest;
import com.hokyozu.kyofuse.users.dto.response.UserAccountResponse;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserFinder userFinder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailCipherService emailCipherService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private SteamService steamService;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchSessionRepository accountSwitchSessionRepository;

    @Mock
    private com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AccountSuccessionService accountSuccessionService;

    @InjectMocks
    private UserAccountService userAccountService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .email("john@example.com")
                .emailIndex("blind_john")
                .passwordHash("hashed_old_pwd")
                .hasCustomPassword(true)
                .totpEnabled(false)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void getAccountReturnsUserAccountResponse() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        UserAccountResponse response = userAccountService.getAccount(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("johndoe");
        assertThat(response.email()).isEqualTo("john@example.com");
        assertThat(response.hasPassword()).isTrue();
    }

    @Test
    void updateUsernameSuccess() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userRepository.existsByUsernameIgnoreCaseAndEmailVerifiedTrue("newname")).thenReturn(false);

        UserAccountResponse response = userAccountService.updateUsername(userId, new UpdateUsernameRequest("newname"));

        assertThat(response.username()).isEqualTo("newname");
        assertThat(user.getUsername()).isEqualTo("newname");
        verify(userRepository).save(user);
    }

    @Test
    void updateUsernameThrowsWhenUsernameAlreadyTaken() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(userRepository.existsByUsernameIgnoreCaseAndEmailVerifiedTrue("takenname")).thenReturn(true);

        assertThatThrownBy(() -> userAccountService.updateUsername(userId, new UpdateUsernameRequest("takenname")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("já está em uso");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateEmailSuccessWithPasswordValidation() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(emailCipherService.blindIndex("newemail@example.com")).thenReturn("blind_new");
        when(passwordEncoder.matches("oldPassword123", "hashed_old_pwd")).thenReturn(true);
        when(userRepository.existsByEmailIndexAndEmailVerifiedTrue("blind_new")).thenReturn(false);

        UserAccountResponse response = userAccountService.updateEmail(
                userId,
                new UpdateEmailRequest("newemail@example.com", "oldPassword123")
        );

        assertThat(response.email()).isEqualTo("john@example.com");
        verify(emailVerificationService).createLinkVerificationToken(user, "newemail@example.com", "blind_new");
    }

    @Test
    void updateEmailThrowsWhenGoogleIsLinked() {
        user.setGoogleId("google-123");
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> userAccountService.updateEmail(
                userId,
                new UpdateEmailRequest("newemail@example.com", "oldPassword123")
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("vinculado ao Google");

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordSuccess() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(passwordEncoder.matches("oldPassword123", "hashed_old_pwd")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashed_new_pwd");

        userAccountService.changePassword(userId, new ChangePasswordRequest("oldPassword123", "newPassword123"));

        assertThat(user.getPasswordHash()).isEqualTo("hashed_new_pwd");
        assertThat(user.isHasCustomPassword()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void linkGoogleSuccess() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("google-sub-789");
        payload.setEmail("john@example.com");

        when(googleTokenVerifierService.verify("id-token-123")).thenReturn(payload);
        when(emailCipherService.blindIndex("john@example.com")).thenReturn("blind_john");
        when(userRepository.findByGoogleId("google-sub-789")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIndex("blind_john")).thenReturn(Optional.of(user));

        UserAccountResponse response = userAccountService.linkGoogle(userId, "id-token-123");

        assertThat(response.hasGoogle()).isTrue();
        assertThat(user.getGoogleId()).isEqualTo("google-sub-789");
        verify(userRepository).save(user);
    }

    @Test
    void unlinkGoogleSuccessWhenRequirementsMet() {
        user.setGoogleId("google-sub-789");
        user.setHasCustomPassword(true);
        user.setEmailVerified(true);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        UserAccountResponse response = userAccountService.unlinkGoogle(userId);

        assertThat(response.hasGoogle()).isFalse();
        assertThat(user.getGoogleId()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void unlinkGoogleFailsWhenNoCustomPassword() {
        user.setGoogleId("google-sub-789");
        user.setHasCustomPassword(false);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> userAccountService.unlinkGoogle(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("senha própria");

        verify(userRepository, never()).save(any());
    }

    @Test
    void linkSteamSuccess() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        Map<String, String> openIdParams = Map.of("openid.mode", "id_res");

        when(steamService.validateOpenIdAndGetSteamId(openIdParams)).thenReturn("76561198012345678");
        when(userRepository.findBySteamId("76561198012345678")).thenReturn(Optional.empty());

        UserAccountResponse response = userAccountService.linkSteam(userId, openIdParams);

        assertThat(response.hasSteam()).isTrue();
        assertThat(user.getSteamId()).isEqualTo("76561198012345678");
        verify(userRepository).save(user);
    }

    @Test
    void unlinkSteamSuccessWhenRequirementsMet() {
        user.setSteamId("76561198012345678");
        user.setHasCustomPassword(true);
        user.setEmail("john@example.com");
        user.setEmailVerified(true);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        UserAccountResponse response = userAccountService.unlinkSteam(userId);

        assertThat(response.hasSteam()).isFalse();
        assertThat(user.getSteamId()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void unlinkSteamFailsWhenSyntheticEmail() {
        user.setSteamId("76561198012345678");
        user.setHasCustomPassword(true);
        user.setEmail("steam_76561198012345678@steam.kyofuse.local");
        user.setEmailVerified(true);
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);

        assertThatThrownBy(() -> userAccountService.unlinkSteam(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("e-mail próprio");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateAccountSuccess() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(passwordEncoder.matches("myPassword", user.getPasswordHash())).thenReturn(true);

        userAccountService.deactivateAccount(userId, new com.hokyozu.kyofuse.users.dto.request.DeactivateAccountRequest("myPassword", "Pausing gaming"));

        assertThat(user.getStatus()).isEqualTo(com.hokyozu.kyofuse.users.enums.UserStatus.INACTIVE);
        assertThat(user.getDeactivatedAt()).isNotNull();
        assertThat(user.getDeletionScheduledAt()).isNull();

        verify(accountSuccessionService).handleOwnershipTransferAndDemotion(user);
        verify(accountSwitchSessionRepository).deleteAllByUserId(userId);
        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(userRepository).save(user);
    }

    @Test
    void scheduleDeletionSuccess() {
        when(userFinder.findProfileByUserId(userId)).thenReturn(user);
        when(passwordEncoder.matches("myPassword", user.getPasswordHash())).thenReturn(true);

        userAccountService.scheduleDeletion(userId, new com.hokyozu.kyofuse.users.dto.request.ScheduleDeletionRequest("myPassword", "Leaving platform"));

        assertThat(user.getStatus()).isEqualTo(com.hokyozu.kyofuse.users.enums.UserStatus.INACTIVE);
        assertThat(user.getDeactivatedAt()).isNotNull();
        assertThat(user.getDeletionScheduledAt()).isNotNull();

        verify(accountSuccessionService).handleOwnershipTransferAndDemotion(user);
        verify(accountSwitchSessionRepository).deleteAllByUserId(userId);
        verify(refreshTokenRepository).deleteAllByUser(user);
        verify(userRepository).save(user);
    }
}
