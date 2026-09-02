package com.hokyozu.kyofuse.infrastructure.security.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailEncryptionBackfillRunnerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private EmailCipherService emailCipherService;

    @InjectMocks
    private EmailEncryptionBackfillRunner runner;

    @Test
    void encryptsAndBackfillsEveryPendingRow() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();

        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("id", firstId, "email", "hideo@example.com"),
                Map.of("id", secondId, "email", "solid@example.com")
        ));
        when(emailCipherService.encrypt("hideo@example.com")).thenReturn("cipher-1");
        when(emailCipherService.blindIndex("hideo@example.com")).thenReturn("index-1");
        when(emailCipherService.encrypt("solid@example.com")).thenReturn("cipher-2");
        when(emailCipherService.blindIndex("solid@example.com")).thenReturn("index-2");

        runner.run(null);

        verify(jdbcTemplate).update(anyString(), eq("cipher-1"), eq("index-1"), eq(firstId));
        verify(jdbcTemplate).update(anyString(), eq("cipher-2"), eq("index-2"), eq(secondId));
    }

    @Test
    void doesNothingWhenThereIsNoPendingRow() {
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

        runner.run(null);

        verify(jdbcTemplate, never()).update(anyString(), any(), any(), any());
    }
}
