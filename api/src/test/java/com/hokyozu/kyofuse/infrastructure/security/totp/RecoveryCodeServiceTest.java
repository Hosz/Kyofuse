package com.hokyozu.kyofuse.infrastructure.security.totp;

import com.hokyozu.kyofuse.infrastructure.entity.RecoveryCode;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveryCodeServiceTest {

    @Mock
    private RecoveryCodeRepository recoveryCodeRepository;

    private RecoveryCodeService recoveryCodeService;

    private User user;

    @BeforeEach
    void setUp() {
        recoveryCodeService = new RecoveryCodeService(recoveryCodeRepository);
        user = User.builder().id(UUID.randomUUID()).role(UserRole.USER).build();
    }

    @Test
    void regenerateDeletesOldCodesAndPersistsEightNewHashedUniqueCodes() {
        List<String> rawCodes = recoveryCodeService.regenerate(user);

        verify(recoveryCodeRepository).deleteAllByUser(user);

        ArgumentCaptor<List<RecoveryCode>> captor = ArgumentCaptor.forClass(List.class);
        verify(recoveryCodeRepository).saveAll(captor.capture());

        List<RecoveryCode> persisted = captor.getValue();
        assertThat(rawCodes).hasSize(8);
        assertThat(persisted).hasSize(8);
        assertThat(rawCodes).doesNotHaveDuplicates();

        Set<String> persistedHashes = persisted.stream().map(RecoveryCode::getCodeHash).collect(Collectors.toSet());
        assertThat(persistedHashes).hasSize(8);
        assertThat(persisted).allSatisfy(code -> {
            assertThat(code.getUser()).isEqualTo(user);
            assertThat(code.getCodeHash()).hasSize(64);
        });
    }

    @Test
    void consumeMarksMatchingUnusedCodeAsUsedAndReturnsTrue() {
        RecoveryCode stored = RecoveryCode.builder()
                .id(UUID.randomUUID())
                .user(user)
                .codeHash("irrelevant-because-repository-is-mocked")
                .createdAt(Instant.now())
                .build();

        when(recoveryCodeRepository.findByUserAndCodeHashAndUsedAtIsNull(eq(user), any()))
                .thenReturn(Optional.of(stored));

        boolean consumed = recoveryCodeService.consume(user, "ABCDE-12345");

        assertThat(consumed).isTrue();
        assertThat(stored.getUsedAt()).isNotNull();
        verify(recoveryCodeRepository).save(stored);
    }

    @Test
    void consumeReturnsFalseWhenNoMatchingUnusedCodeExists() {
        when(recoveryCodeRepository.findByUserAndCodeHashAndUsedAtIsNull(eq(user), any()))
                .thenReturn(Optional.empty());

        assertThat(recoveryCodeService.consume(user, "ABCDE-12345")).isFalse();
        verify(recoveryCodeRepository, never()).save(any());
    }

    @Test
    void consumeReturnsFalseForBlankCodeWithoutHittingTheRepository() {
        assertThat(recoveryCodeService.consume(user, " ")).isFalse();
        assertThat(recoveryCodeService.consume(user, null)).isFalse();

        verify(recoveryCodeRepository, never()).findByUserAndCodeHashAndUsedAtIsNull(any(), any());
    }

    @Test
    void deleteAllDelegatesToRepository() {
        recoveryCodeService.deleteAll(user);

        verify(recoveryCodeRepository).deleteAllByUser(user);
    }
}
