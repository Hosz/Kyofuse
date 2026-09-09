package com.hokyozu.kyofuse.users.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.auth.service.EmailVerificationService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AccountSwitchSessionRepository;
import com.hokyozu.kyofuse.infrastructure.security.oauth.GoogleTokenVerifierService;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamService;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.dto.request.ChangePasswordRequest;
import com.hokyozu.kyofuse.users.dto.request.UpdateEmailRequest;
import com.hokyozu.kyofuse.users.dto.request.UpdateUsernameRequest;
import com.hokyozu.kyofuse.users.dto.response.UserAccountResponse;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.mapper.UserAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserFinder userFinder;
    private final UserRepository userRepository;
    private final EmailCipherService emailCipherService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final SteamService steamService;
    private final AccountSwitchSessionRepository accountSwitchSessionRepository;
    private final com.hokyozu.kyofuse.infrastructure.security.jwt.RefreshTokenRepository refreshTokenRepository;
    private final AccountSuccessionService accountSuccessionService;

    @Transactional(readOnly = true)
    public UserAccountResponse getAccount(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        return UserAccountMapper.toResponse(user);
    }

    @Transactional
    public UserAccountResponse updateUsername(UUID userId, UpdateUsernameRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        String targetUsername = request.username().trim();

        if (user.getUsername().equalsIgnoreCase(targetUsername)) {
            return UserAccountMapper.toResponse(user);
        }

        if (userRepository.existsByUsernameIgnoreCaseAndEmailVerifiedTrue(targetUsername)) {
            throw new ConflictException("Este nome de usuário já está em uso.");
        }

        user.setUsername(targetUsername);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return UserAccountMapper.toResponse(user);
    }

    @Transactional
    public UserAccountResponse updateEmail(UUID userId, UpdateEmailRequest request) {
        User user = userFinder.findProfileByUserId(userId);

        if (user.getGoogleId() != null && !user.getGoogleId().isBlank()) {
            throw new BadRequestException("O e-mail da sua conta está vinculado ao Google e não pode ser alterado diretamente. Desvincule a conta Google primeiro.");
        }

        String targetEmail = request.email().trim().toLowerCase();
        String targetEmailIndex = emailCipherService.blindIndex(targetEmail);

        if (targetEmailIndex.equals(user.getEmailIndex())) {
            return UserAccountMapper.toResponse(user);
        }

        if (user.isHasCustomPassword()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new BadRequestException("Informe sua senha atual para alterar o e-mail.");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new UnauthorizedException("Senha atual incorreta.");
            }
        }

        if (userRepository.existsByEmailIndexAndEmailVerifiedTrue(targetEmailIndex)) {
            throw new ConflictException("Este e-mail já está associado a outra conta.");
        }

        emailVerificationService.createLinkVerificationToken(user, targetEmail, targetEmailIndex);

        return UserAccountMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userFinder.findProfileByUserId(userId);

        if (user.isHasCustomPassword()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new BadRequestException("Informe sua senha atual.");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new UnauthorizedException("Senha atual incorreta.");
            }
        }

        if (request.newPassword().length() < 8) {
            throw new BadRequestException("A nova senha deve ter no mínimo 8 caracteres.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setHasCustomPassword(true);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        accountSwitchSessionRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public UserAccountResponse linkGoogle(UUID userId, String idToken) {
        User user = userFinder.findProfileByUserId(userId);

        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(idToken);
        String googleId = payload.getSubject();
        String googleEmail = payload.getEmail();
        String googleEmailIndex = emailCipherService.blindIndex(googleEmail);

        Optional<User> byGoogleId = userRepository.findByGoogleId(googleId);
        if (byGoogleId.isPresent() && !byGoogleId.get().getId().equals(userId)) {
            throw new ConflictException("Esta conta Google já está vinculada a outro usuário.");
        }

        Optional<User> byEmail = userRepository.findByEmailIndex(googleEmailIndex);
        if (byEmail.isPresent() && !byEmail.get().getId().equals(userId)) {
            throw new ConflictException("O e-mail desta conta Google já está cadastrado em outra conta.");
        }

        user.setGoogleId(googleId);

        if (isSyntheticSteamEmail(user.getEmail())) {
            user.setEmail(googleEmail);
            user.setEmailIndex(googleEmailIndex);
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(Instant.now());
        }

        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return UserAccountMapper.toResponse(user);
    }

    @Transactional
    public UserAccountResponse unlinkGoogle(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);

        if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
            throw new BadRequestException("Nenhuma conta Google está vinculada ao seu perfil.");
        }

        if (!user.isHasCustomPassword()) {
            throw new BadRequestException("Para desvincular a conta Google, você precisa cadastrar uma senha própria primeiro.");
        }

        if (isSyntheticSteamEmail(user.getEmail())) {
            throw new BadRequestException("Para desvincular a conta Google, você precisa possuir um e-mail válido.");
        }

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Para desvincular a conta Google, seu e-mail cadastrado precisa estar verificado.");
        }

        user.setGoogleId(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return UserAccountMapper.toResponse(user);
    }

    @Transactional
    public UserAccountResponse linkSteam(UUID userId, Map<String, String> openIdParams) {
        User user = userFinder.findProfileByUserId(userId);

        String steamId = steamService.validateOpenIdAndGetSteamId(openIdParams);

        Optional<User> bySteamId = userRepository.findBySteamId(steamId);
        if (bySteamId.isPresent() && !bySteamId.get().getId().equals(userId)) {
            throw new ConflictException("Esta conta Steam já está vinculada a outro usuário.");
        }

        user.setSteamId(steamId);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return UserAccountMapper.toResponse(user);
    }

    @Transactional
    public UserAccountResponse unlinkSteam(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);

        if (user.getSteamId() == null || user.getSteamId().isBlank()) {
            throw new BadRequestException("Nenhuma conta Steam está vinculada ao seu perfil.");
        }

        if (!user.isHasCustomPassword()) {
            throw new BadRequestException("Para desvincular a conta Steam, você precisa cadastrar uma senha própria primeiro.");
        }

        if (isSyntheticSteamEmail(user.getEmail())) {
            throw new BadRequestException("Para desvincular a conta Steam, você precisa cadastrar um e-mail próprio.");
        }

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Para desvincular a conta Steam, seu e-mail cadastrado precisa estar verificado.");
        }

        user.setSteamId(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        return UserAccountMapper.toResponse(user);
    }

    @Transactional
    public void deactivateAccount(UUID userId, com.hokyozu.kyofuse.users.dto.request.DeactivateAccountRequest request) {
        User user = userFinder.findProfileByUserId(userId);

        if (user.isHasCustomPassword()) {
            if (request.password() == null || request.password().isBlank()) {
                throw new BadRequestException("Informe sua senha para confirmar a desativação da conta.");
            }
            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new UnauthorizedException("Senha incorreta.");
            }
        }

        user.setStatus(com.hokyozu.kyofuse.users.enums.UserStatus.INACTIVE);
        user.setDeactivatedAt(Instant.now());
        user.setDeletionScheduledAt(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        accountSuccessionService.handleOwnershipTransferAndDemotion(user);

        accountSwitchSessionRepository.deleteAllByUserId(userId);
        refreshTokenRepository.deleteAllByUser(user);
    }

    @Transactional
    public void scheduleDeletion(UUID userId, com.hokyozu.kyofuse.users.dto.request.ScheduleDeletionRequest request) {
        User user = userFinder.findProfileByUserId(userId);

        if (user.isHasCustomPassword()) {
            if (request.password() == null || request.password().isBlank()) {
                throw new BadRequestException("Informe sua senha para confirmar a exclusão da conta.");
            }
            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new UnauthorizedException("Senha incorreta.");
            }
        }

        user.setStatus(com.hokyozu.kyofuse.users.enums.UserStatus.INACTIVE);
        user.setDeactivatedAt(Instant.now());
        user.setDeletionScheduledAt(Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        accountSuccessionService.handleOwnershipTransferAndDemotion(user);

        accountSwitchSessionRepository.deleteAllByUserId(userId);
        refreshTokenRepository.deleteAllByUser(user);
    }

    private boolean isSyntheticSteamEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@steam.kyofuse.local");
    }
}
