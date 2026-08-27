package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.response.TotpSetupResponse;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.totp.RecoveryCodeService;
import com.hokyozu.kyofuse.infrastructure.security.totp.TotpService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class TwoFactorAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TotpService totpService;

    @Mock
    private RecoveryCodeService recoveryCodeService;

    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    @Test
    void setupGeneratesAndStoresAPendingUnconfirmedSecret() {
        User user = User.builder().id(UUID.randomUUID()).username("hideo").totpEnabled(true).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(totpService.generateSecret()).thenReturn("SECRET123");
        when(totpService.buildOtpAuthUri("hideo", "SECRET123")).thenReturn("otpauth://totp/Kyofuse:hideo");

        TotpSetupResponse response = twoFactorAuthService.setup(user.getId());

        assertThat(response.secret()).isEqualTo("SECRET123");
        assertThat(response.otpauthUri()).isEqualTo("otpauth://totp/Kyofuse:hideo");
        assertThat(user.getTotpSecret()).isEqualTo("SECRET123");
        assertThat(user.isTotpEnabled()).isFalse();
        assertThat(user.getTotpConfirmedAt()).isNull();
        verify(userRepository).save(user);
    }

    @Test
    void setupThrowsWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> twoFactorAuthService.setup(userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void confirmEnablesTotpAndReturnsRecoveryCodesWhenCodeIsValid() {
        User user = User.builder().id(UUID.randomUUID()).totpSecret("SECRET123").totpEnabled(false).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(totpService.verifyCode("SECRET123", "123456")).thenReturn(true);
        when(recoveryCodeService.regenerate(user)).thenReturn(List.of("AAAAA-11111", "BBBBB-22222"));

        List<String> recoveryCodes = twoFactorAuthService.confirm(user.getId(), "123456");

        assertThat(recoveryCodes).containsExactly("AAAAA-11111", "BBBBB-22222");
        assertThat(user.isTotpEnabled()).isTrue();
        assertThat(user.getTotpConfirmedAt()).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void confirmRejectsAnInvalidCodeAndDoesNotEnableTotp() {
        User user = User.builder().id(UUID.randomUUID()).totpSecret("SECRET123").totpEnabled(false).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(totpService.verifyCode("SECRET123", "000000")).thenReturn(false);

        assertThatThrownBy(() -> twoFactorAuthService.confirm(user.getId(), "000000"))
                .isInstanceOf(UnauthorizedException.class);

        assertThat(user.isTotpEnabled()).isFalse();
        verify(userRepository, never()).save(any());
        verify(recoveryCodeService, never()).regenerate(any());
    }

    @Test
    void confirmRejectsWhenThereIsNoPendingSetup() {
        User user = User.builder().id(UUID.randomUUID()).totpSecret(null).build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> twoFactorAuthService.confirm(user.getId(), "123456"))
                .isInstanceOf(BadRequestException.class);

        verify(totpService, never()).verifyCode(any(), any());
    }

    @Test
    void disableClearsTotpStateAndRecoveryCodesWhenPasswordMatches() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .passwordHash("hashed-password")
                .totpSecret("SECRET123")
                .totpEnabled(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "hashed-password")).thenReturn(true);

        twoFactorAuthService.disable(user.getId(), "correct-password");

        assertThat(user.getTotpSecret()).isNull();
        assertThat(user.isTotpEnabled()).isFalse();
        assertThat(user.getTotpConfirmedAt()).isNull();
        verify(userRepository).save(user);
        verify(recoveryCodeService).deleteAll(user);
    }

    @Test
    void disableRejectsWrongPasswordAndKeepsTotpEnabled() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .passwordHash("hashed-password")
                .totpSecret("SECRET123")
                .totpEnabled(true)
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> twoFactorAuthService.disable(user.getId(), "wrong-password"))
                .isInstanceOf(UnauthorizedException.class);

        assertThat(user.isTotpEnabled()).isTrue();
        verify(userRepository, never()).save(any());
        verify(recoveryCodeService, never()).deleteAll(any());
    }
}
