package com.hokyozu.kyofuse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.from:Kyofuse <noreply@kyofuse.com>}")
    private String fromEmail;

    private static final java.time.format.DateTimeFormatter FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss (z)")
            .withZone(java.time.ZoneId.of("UTC"));

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/recuperar-senha?token=" + token;

        log.info("[PasswordReset] Link de recuperação gerado para {}: {}", toEmail, resetLink);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Kyofuse - Recuperação de Senha");
            message.setText("Olá,\n\nRecebemos uma solicitação para redefinir a senha da sua conta.\n"
                    + "Acesse o link abaixo para criar uma nova senha (válido por 15 minutos):\n\n"
                    + resetLink + "\n\nSe você não solicitou isso, ignore este e-mail.");

            mailSender.send(message);
            log.info("[PasswordReset] E-mail enviado com sucesso para {}", toEmail);
        } catch (MailException e) {
            log.error("[PasswordReset] Falha ao enviar e-mail via SMTP para {}. Causa: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendEmailVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/verificar-email?token=" + token;

        log.info("[EmailVerification] Link de verificação gerado para {}: {}", toEmail, verificationLink);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Kyofuse - Confirmação de E-mail");
            message.setText("Olá,\n\nObrigado por se cadastrar na Kyofuse!\n"
                    + "Para ativar sua conta, por favor confirme seu e-mail acessando o link abaixo:\n\n"
                    + verificationLink + "\n\nEste link é válido por 24 horas.\nSe você não realizou este cadastro, ignore este e-mail.");

            mailSender.send(message);
            log.info("[EmailVerification] E-mail enviado com sucesso para {}", toEmail);
        } catch (MailException e) {
            log.error("[EmailVerification] Falha ao enviar e-mail via SMTP para {}. Causa: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendLoginSecurityAlertEmail(String toEmail, String username, String location, String device, String ip, java.time.Instant loggedAt) {
        if (toEmail == null || toEmail.isBlank() || toEmail.endsWith("@steam.kyofuse.local")) {
            return;
        }

        String formattedTime = loggedAt != null ? FORMATTER.format(loggedAt) : "Agora";
        log.info("[LoginSecurity] Alerta de novo login para {}: {} | {} | {}", toEmail, location, device, ip);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Kyofuse - Novo login detectado na sua conta");
            message.setText(String.format(
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
            ));

            mailSender.send(message);
            log.info("[LoginSecurity] E-mail de alerta de login enviado com sucesso para {}", toEmail);
        } catch (MailException e) {
            log.error("[LoginSecurity] Falha ao enviar e-mail de alerta de login para {}. Causa: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendAccountReactivationEmail(String toEmail, String code, boolean scheduledDeletion, java.time.Instant scheduledDeletionDate) {
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

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(messageBody);

            mailSender.send(message);
            log.info("[AccountReactivation] E-mail enviado com sucesso para {}", toEmail);
        } catch (MailException e) {
            log.error("[AccountReactivation] Falha ao enviar e-mail para {}. Causa: {}", toEmail, e.getMessage());
        }
    }
}
