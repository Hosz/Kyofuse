package com.hokyozu.kyofuse.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.from:Kyofuse <noreply@kyofuse.com>}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss (z)")
            .withZone(java.time.ZoneId.of("UTC"));

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/recuperar-senha?token=" + token;

        log.info("[PasswordReset] Link de recuperação gerado para {}: {}", toEmail, resetLink);

        String text = "Olá,\n\nRecebemos uma solicitação para redefinir a senha da sua conta.\n"
                + "Acesse o link abaixo para criar uma nova senha (válido por 15 minutos):\n\n"
                + resetLink + "\n\nSe você não solicitou isso, ignore este e-mail.";

        sendMail(toEmail, "Kyofuse - Recuperação de Senha", text, "PasswordReset");
    }

    @Async
    public void sendEmailVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/verificar-email?token=" + token;

        log.info("[EmailVerification] Link de verificação gerado para {}: {}", toEmail, verificationLink);

        String text = "Olá,\n\nObrigado por se cadastrar na Kyofuse!\n"
                + "Para ativar sua conta, por favor confirme seu e-mail acessando o link abaixo:\n\n"
                + verificationLink + "\n\nEste link é válido por 24 horas.\nSe você não realizou este cadastro, ignore este e-mail.";

        sendMail(toEmail, "Kyofuse - Confirmação de E-mail", text, "EmailVerification");
    }

    @Async
    public void sendEmailLinkVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/verificar-email?token=" + token;

        log.info("[EmailLinkVerification] Link de vinculação de e-mail gerado para {}: {}", toEmail, verificationLink);

        String text = "Olá,\n\nRecebemos uma solicitação para vincular este endereço de e-mail à sua conta Kyofuse.\n"
                + "Para confirmar e ativar este e-mail na sua conta, acesse o link abaixo:\n\n"
                + verificationLink + "\n\nEste link é válido por 24 horas.\nSe você não solicitou esta alteração, ignore este e-mail.";

        sendMail(toEmail, "Kyofuse - Confirmação de Vinculação de E-mail", text, "EmailLinkVerification");
    }

    @Async
    public void sendLoginSecurityAlertEmail(String toEmail, String username, String location, String device, String ip, Instant loggedAt) {
        if (toEmail == null || toEmail.isBlank() || toEmail.endsWith("@steam.kyofuse.local")) {
            return;
        }

        String formattedTime = loggedAt != null ? FORMATTER.format(loggedAt) : "Agora";
        log.info("[LoginSecurity] Alerta de novo login para {}: {} | {} | {}", toEmail, location, device, ip);

        String text = String.format(
                "Olá, %s,\n\n"
                + "Detectamos um novo acesso à sua conta Kyofuse com as seguintes informações:\n\n"
                + "• Localização aproximada: %s\n"
                + "• Dispositivo/Navegador: %s\n"
                + "• Endereço IP: %s\n"
                + "• Data e Hora: %s\n\n"
                + "Se foi você, não é necessário fazer nada.\n\n"
                + "Caso NÃO tenha sido você, recomendamos que altere sua senha imediatamente em:\n"
                + "%s/recuperar-senha\n\n"
                + "Equipe de Segurança Kyofuse",
                (username != null ? username : "Jogador"),
                (location != null ? location : "Localização não identificada"),
                (device != null ? device : "Dispositivo desconhecido"),
                (ip != null ? ip : "Desconhecido"),
                formattedTime,
                frontendUrl
        );

        sendMail(toEmail, "Kyofuse - Novo login detectado na sua conta", text, "LoginSecurity");
    }

    @Async
    public void sendAccountReactivationEmail(String toEmail, String code, boolean scheduledDeletion, Instant scheduledDeletionDate) {
        if (toEmail == null || toEmail.isBlank() || toEmail.endsWith("@steam.kyofuse.local")) {
            return;
        }

        log.info("[AccountReactivation] Código de reativação gerado para {}: {}", toEmail, code);

        String subject = scheduledDeletion
                ? "Kyofuse - Cancelamento de Exclusão e Reativação de Conta"
                : "Kyofuse - Código de Reativação de Conta";

        String messageBody = scheduledDeletion
                ? String.format(
                        "Olá,\n\n"
                        + "Recebemos uma solicitação para reativar sua conta Kyofuse e cancelar o agendamento de exclusão definitiva (prevista para %s).\n\n"
                        + "Seu código de segurança para reativação é:\n\n"
                        + "   %s\n\n"
                        + "Este código é válido por 15 minutos.\n"
                        + "Se você não solicitou a reativação da sua conta, nenhuma ação é necessária e o processo de exclusão continuará normalmente.\n\n"
                        + "Equipe Kyofuse",
                        (scheduledDeletionDate != null ? FORMATTER.format(scheduledDeletionDate) : "em breve"),
                        code
                )
                : String.format(
                        "Olá,\n\n"
                        + "Recebemos uma solicitação para reativar sua conta Kyofuse.\n\n"
                        + "Seu código de segurança para reativação é:\n\n"
                        + "   %s\n\n"
                        + "Este código é válido por 15 minutos.\n"
                        + "Insira o código acima no formulário de login para reativar sua conta instantaneamente.\n\n"
                        + "Equipe Kyofuse",
                        code
                );

        sendMail(toEmail, subject, messageBody, "AccountReactivation");
    }

    private void sendMail(String toEmail, String subject, String body, String logPrefix) {
        if (isResendConfigured()) {
            boolean success = sendViaResendApi(toEmail, subject, body, logPrefix);
            if (success) {
                return;
            }
            log.warn("[{}] Falha no envio via Resend HTTP API. Tentando fallback via SMTP...", logPrefix);
        }

        sendViaSmtp(toEmail, subject, body, logPrefix);
    }

    private boolean isResendConfigured() {
        return mailPassword != null && mailPassword.trim().startsWith("re_");
    }

    private boolean sendViaResendApi(String toEmail, String subject, String body, String logPrefix) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            Map<String, Object> payload = Map.of(
                    "from", fromEmail,
                    "to", List.of(toEmail),
                    "subject", subject,
                    "text", body
            );

            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + mailPassword.trim())
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Kyofuse-Server/1.0")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[{}] E-mail enviado com sucesso via Resend API para {}: status={}", logPrefix, toEmail, response.statusCode());
                return true;
            } else {
                log.error("[{}] Erro retornado pela Resend API para {}: status={}, resposta={}", logPrefix, toEmail, response.statusCode(), response.body());
                return false;
            }
        } catch (Exception e) {
            log.error("[{}] Exceção ao enviar e-mail via Resend API para {}: {}", logPrefix, toEmail, e.getMessage());
            return false;
        }
    }

    private void sendViaSmtp(String toEmail, String subject, String body, String logPrefix) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("[{}] E-mail enviado com sucesso via SMTP para {}", logPrefix, toEmail);
        } catch (MailException e) {
            log.error("[{}] Falha ao enviar e-mail via SMTP para {}. Causa: {}", logPrefix, toEmail, e.getMessage());
        }
    }
}
