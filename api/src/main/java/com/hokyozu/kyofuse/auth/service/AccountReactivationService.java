package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.response.ReactivationRequiredResponse;
import com.hokyozu.kyofuse.auth.entity.AccountReactivationCode;
import com.hokyozu.kyofuse.auth.repository.AccountReactivationCodeRepository;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.reactivation.AccountReactivationTokenService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountReactivationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(15);
    private static final int MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final AccountReactivationCodeRepository reactivationCodeRepository;
    private final AccountReactivationTokenService reactivationTokenService;
    private final MailService mailService;
    private final com.hokyozu.kyofuse.users.service.AccountSuccessionService accountSuccessionService;

    @Transactional
    public ReactivationRequiredResponse createAndSendReactivationCode(User user) {
        reactivationCodeRepository.deleteAllByUser(user);

        int numericCode = 100_000 + new SecureRandom().nextInt(900_000);
        String rawCode = String.valueOf(numericCode);
        String codeHash = hashCode(rawCode);

        AccountReactivationCode reactivationCode = AccountReactivationCode.builder()
                .user(user)
                .codeHash(codeHash)
                .attempts(0)
                .expiresAt(Instant.now().plus(CODE_TTL))
                .createdAt(Instant.now())
                .build();

        reactivationCodeRepository.save(reactivationCode);

        boolean isScheduledDeletion = user.getDeletionScheduledAt() != null;
        mailService.sendAccountReactivationEmail(user.getEmail(), rawCode, isScheduledDeletion, user.getDeletionScheduledAt());

        String reactivationToken = reactivationTokenService.generate(user);
        String maskedEmail = maskEmail(user.getEmail());

        return new ReactivationRequiredResponse(
                true,
                reactivationToken,
                maskedEmail,
                isScheduledDeletion,
                user.getDeletionScheduledAt()
        );
    }

    @Transactional
    public void resendReactivationCode(String reactivationToken) {
        var userId = reactivationTokenService.resolveUserId(reactivationToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        createAndSendReactivationCode(user);
    }

    @Transactional
    public User confirmReactivation(String reactivationToken, String rawCode) {
        var userId = reactivationTokenService.resolveUserId(reactivationToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        AccountReactivationCode codeEntity = reactivationCodeRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("Nenhum código de reativação ativo encontrado. Solicite um novo código."));

        if (codeEntity.isUsed()) {
            throw new BadRequestException("Este código já foi utilizado. Solicite um novo código.");
        }

        if (codeEntity.isExpired()) {
            throw new BadRequestException("O código de reativação expirou. Solicite um novo código.");
        }

        if (codeEntity.getAttempts() >= MAX_ATTEMPTS) {
            throw new BadRequestException("Número máximo de tentativas excedido. Solicite um novo código.");
        }

        String inputHash = hashCode(rawCode.trim());
        if (!inputHash.equals(codeEntity.getCodeHash())) {
            codeEntity.setAttempts(codeEntity.getAttempts() + 1);
            reactivationCodeRepository.save(codeEntity);
            throw new BadRequestException("Código de verificação incorreto.");
        }

        codeEntity.setUsedAt(Instant.now());
        reactivationCodeRepository.save(codeEntity);

        user.setStatus(UserStatus.ACTIVE);
        user.setDeactivatedAt(null);
        user.setDeletionScheduledAt(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        accountSuccessionService.handleAccountReactivation(user);

        log.info("[AccountReactivation] Conta {} reativada com sucesso.", user.getUsername());
        return user;
    }

    private String hashCode(String rawCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawCode.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Erro ao calcular hash do código de reativação.", e);
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "seu e-mail cadastrado";
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }
}
