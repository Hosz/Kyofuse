package com.hokyozu.kyofuse.auth.service;

import com.hokyozu.kyofuse.auth.dto.response.TotpSetupResponse;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.totp.RecoveryCodeService;
import com.hokyozu.kyofuse.infrastructure.security.totp.TotpService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Gerencia o ciclo de vida do 2FA de uma conta já autenticada (ativar, confirmar,
 * desativar). O fluxo de login em si — exigir e validar o código na hora de entrar —
 * fica no {@link AuthService}, junto com o resto da autenticação.
 */
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;
    private final RecoveryCodeService recoveryCodeService;

    @Transactional
    public TotpSetupResponse setup(UUID userId) {
        User user = findUser(userId);
        String secret = totpService.generateSecret();

        user.setTotpSecret(secret);
        user.setTotpEnabled(false);
        user.setTotpConfirmedAt(null);
        userRepository.save(user);

        return new TotpSetupResponse(secret, totpService.buildOtpAuthUri(user.getUsername(), secret));
    }

    @Transactional
    public List<String> confirm(UUID userId, String code) {
        User user = findUser(userId);

        if (user.getTotpSecret() == null) {
            throw new BadRequestException("Nenhum setup de 2FA em andamento.");
        }

        if (!totpService.verifyCode(user.getTotpSecret(), code)) {
            throw new UnauthorizedException("Código de verificação inválido.");
        }

        user.setTotpEnabled(true);
        user.setTotpConfirmedAt(Instant.now());
        userRepository.save(user);

        return recoveryCodeService.regenerate(user);
    }

    @Transactional
    public void disable(UUID userId, String rawPassword) {
        User user = findUser(userId);

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Senha inválida.");
        }

        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        user.setTotpConfirmedAt(null);
        userRepository.save(user);

        recoveryCodeService.deleteAll(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + userId));
    }
}
