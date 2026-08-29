package com.hokyozu.kyofuse.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/recuperar-senha?token=" + token;

        log.info("[PasswordReset] Link de recuperação gerado para {}: {}", toEmail, resetLink);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
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

    public void sendEmailVerificationEmail(String toEmail, String token) {
        String verificationLink = frontendUrl + "/verificar-email?token=" + token;

        log.info("[EmailVerification] Link de verificação gerado para {}: {}", toEmail, verificationLink);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
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
}
