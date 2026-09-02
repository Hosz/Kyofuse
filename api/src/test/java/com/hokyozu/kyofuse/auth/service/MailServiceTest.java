package com.hokyozu.kyofuse.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "frontendUrl", "http://localhost:4200");
    }

    @Test
    void sendLoginSecurityAlertEmailSendsMessageSuccessfully() {
        Instant loggedAt = Instant.parse("2026-08-31T12:00:00Z");

        mailService.sendLoginSecurityAlertEmail(
                "gamer@example.com",
                "gamer123",
                "São Paulo, Brasil",
                "Chrome no Windows",
                "189.1.2.3",
                loggedAt
        );

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("gamer@example.com");
        assertThat(message.getSubject()).contains("Novo login detectado");
        assertThat(message.getText()).contains("São Paulo, Brasil");
        assertThat(message.getText()).contains("Chrome no Windows");
        assertThat(message.getText()).contains("189.1.2.3");
    }

    @Test
    void sendLoginSecurityAlertEmailSkipsSyntheticSteamEmail() {
        mailService.sendLoginSecurityAlertEmail(
                "steam_123456789@steam.kyofuse.local",
                "steam_gamer",
                "São Paulo, Brasil",
                "Chrome no Windows",
                "189.1.2.3",
                Instant.now()
        );

        verifyNoInteractions(mailSender);
    }
}
