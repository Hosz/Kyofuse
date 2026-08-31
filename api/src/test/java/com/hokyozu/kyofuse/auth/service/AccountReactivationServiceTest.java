package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.response.ReactivationRequiredResponse;
import com.hokyozu.kyofuse.auth.entity.AccountReactivationCode;
import com.hokyozu.kyofuse.auth.repository.AccountReactivationCodeRepository;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.reactivation.AccountReactivationTokenService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountReactivationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountReactivationCodeRepository reactivationCodeRepository;
    @Mock
    private AccountReactivationTokenService reactivationTokenService;
    @Mock
    private MailService mailService;
    @Mock
    private com.hokyozu.kyofuse.users.service.AccountSuccessionService accountSuccessionService;

    @InjectMocks
    private AccountReactivationService reactivationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("player1")
                .email("player1@example.com")
                .status(UserStatus.INACTIVE)
                .deactivatedAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .build();
    }

    @Test
    @DisplayName("Creates and sends reactivation code successfully")
    void createsAndSendsReactivationCode() {
        when(reactivationTokenService.generate(user)).thenReturn("token123");

        ReactivationRequiredResponse response = reactivationService.createAndSendReactivationCode(user);

        assertTrue(response.reactivationRequired());
        assertEquals("token123", response.reactivationToken());
        assertEquals("p***1@example.com", response.maskedEmail());
        assertFalse(response.scheduledDeletion());

        verify(reactivationCodeRepository).deleteAllByUser(user);
        verify(reactivationCodeRepository).save(any(AccountReactivationCode.class));
        verify(mailService).sendAccountReactivationEmail(eq("player1@example.com"), anyString(), eq(false), isNull());
    }

    @Test
    @DisplayName("Confirms reactivation successfully when valid code is provided")
    void confirmsReactivationSuccessfully() throws Exception {
        String rawCode = "123456";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String codeHash = HexFormat.of().formatHex(digest.digest(rawCode.getBytes(StandardCharsets.UTF_8)));

        AccountReactivationCode codeEntity = AccountReactivationCode.builder()
                .user(user)
                .codeHash(codeHash)
                .attempts(0)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();

        when(reactivationTokenService.resolveUserId("valid-token")).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reactivationCodeRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(codeEntity));

        User reactivated = reactivationService.confirmReactivation("valid-token", rawCode);

        assertEquals(UserStatus.ACTIVE, reactivated.getStatus());
        assertNull(reactivated.getDeactivatedAt());
        assertNull(reactivated.getDeletionScheduledAt());
        assertNotNull(codeEntity.getUsedAt());

        verify(userRepository).save(user);
        verify(reactivationCodeRepository).save(codeEntity);
        verify(accountSuccessionService).handleAccountReactivation(user);
    }

    @Test
    @DisplayName("Throws BadRequestException when invalid code is submitted")
    void throwsWhenInvalidCode() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String codeHash = HexFormat.of().formatHex(digest.digest("999999".getBytes(StandardCharsets.UTF_8)));

        AccountReactivationCode codeEntity = AccountReactivationCode.builder()
                .user(user)
                .codeHash(codeHash)
                .attempts(0)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .createdAt(Instant.now())
                .build();

        when(reactivationTokenService.resolveUserId("valid-token")).thenReturn(user.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reactivationCodeRepository.findTopByUserOrderByCreatedAtDesc(user)).thenReturn(Optional.of(codeEntity));

        assertThrows(BadRequestException.class, () -> reactivationService.confirmReactivation("valid-token", "000000"));
        assertEquals(1, codeEntity.getAttempts());
        verify(reactivationCodeRepository).save(codeEntity);
        verify(userRepository, never()).save(user);
    }
}
