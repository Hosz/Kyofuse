package com.hokyozu.kyofuse.auth.controller;

import com.hokyozu.kyofuse.auth.dto.request.*;
import com.hokyozu.kyofuse.auth.dto.response.*;
import com.hokyozu.kyofuse.auth.mapper.AuthMapper;
import com.hokyozu.kyofuse.auth.service.AccountReactivationService;
import com.hokyozu.kyofuse.auth.service.AuthService;
import com.hokyozu.kyofuse.auth.service.EmailVerificationService;
import com.hokyozu.kyofuse.auth.service.PasswordResetService;
import com.hokyozu.kyofuse.auth.service.TwoFactorAuthService;
import com.hokyozu.kyofuse.infrastructure.client.ClientIpResolver;
import com.hokyozu.kyofuse.infrastructure.ratelimit.RateLimit;
import com.hokyozu.kyofuse.infrastructure.ratelimit.RateLimitType;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamService;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.users.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final AuthCookieService authCookieService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;
    private final SteamService steamService;
    private final GamerProfileRepository gamerProfileRepository;
    private final ClientIpResolver clientIpResolver;
    private final AccountReactivationService accountReactivationService;
    private final com.hokyozu.kyofuse.auth.repository.UserRepository userRepository;
    private final com.hokyozu.kyofuse.infrastructure.security.totp.MfaTokenService mfaTokenService;

    @RateLimit(key = "register", limit = 5, period = 3600, type = RateLimitType.IP)
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = authService.register(request, clientIp(httpRequest));
        return AuthMapper.toRegisterResponse(user);
    }

    @PostMapping("/verify-email")
    public AuthResponse verifyEmail(
            @RequestBody @Valid VerifyEmailRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.verifyEmail(request.token());
        applyAuthCookies(response, result);

        return AuthMapper.toResponse(result.user(), result.switchToken());
    }

    @GetMapping("/verify-email/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateEmailVerificationToken(@RequestParam String token) {
        emailVerificationService.validateToken(token);
    }

    @RateLimit(key = "resend_verification", limit = 3, period = 900, type = RateLimitType.IP)
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public void resendVerification(
            @RequestBody @Valid ResendVerificationEmailRequest request,
            HttpServletRequest httpRequest
    ) {
        emailVerificationService.resendVerification(request.emailOrUsername(), clientIp(httpRequest));
    }

    @RateLimit(key = "login", limit = 5, period = 60, type = RateLimitType.IP)
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.LoginOutcome outcome = authService.login(request, clientIp(httpRequest), userAgent(httpRequest), deviceId(httpRequest));

        return switch (outcome) {
            case AuthService.LoginOutcome.MfaRequired mfaRequired ->
                    ResponseEntity.ok(new MfaRequiredResponse(mfaRequired.mfaToken()));
            case AuthService.LoginOutcome.ReactivationRequired reactivationRequired ->
                    ResponseEntity.ok(new com.hokyozu.kyofuse.auth.dto.response.ReactivationRequiredResponse(
                            reactivationRequired.reactivationToken(),
                            reactivationRequired.maskedEmail(),
                            reactivationRequired.scheduledDeletion(),
                            reactivationRequired.scheduledDeletionDate()
                    ));
            case AuthService.LoginOutcome.Authenticated authenticated -> {
                applyAuthCookies(response, authenticated.result());
                yield ResponseEntity.ok(AuthMapper.toResponse(authenticated.result().user(), authenticated.result().switchToken()));
            }
        };
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(
            @RequestBody @Valid GoogleLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.LoginOutcome outcome = authService.loginWithGoogle(request.idToken(), clientIp(httpRequest), userAgent(httpRequest), deviceId(httpRequest));

        return switch (outcome) {
            case AuthService.LoginOutcome.MfaRequired mfaRequired ->
                    ResponseEntity.ok(new MfaRequiredResponse(mfaRequired.mfaToken()));
            case AuthService.LoginOutcome.ReactivationRequired reactivationRequired ->
                    ResponseEntity.ok(new com.hokyozu.kyofuse.auth.dto.response.ReactivationRequiredResponse(
                            reactivationRequired.reactivationToken(),
                            reactivationRequired.maskedEmail(),
                            reactivationRequired.scheduledDeletion(),
                            reactivationRequired.scheduledDeletionDate()
                    ));
            case AuthService.LoginOutcome.Authenticated authenticated -> {
                applyAuthCookies(response, authenticated.result());
                yield ResponseEntity.ok(AuthMapper.toResponse(authenticated.result().user(), authenticated.result().switchToken()));
            }
        };
    }

    @GetMapping("/steam")
    public ResponseEntity<Void> redirectToSteam(@RequestParam(required = false) String returnUrl) {
        String loginUrl = steamService.buildLoginUrl(returnUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, loginUrl)
                .build();
    }

    @PostMapping("/steam")
    public ResponseEntity<?> loginWithSteam(
            @RequestBody Map<String, String> openIdParams,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.LoginOutcome outcome = authService.loginWithSteam(openIdParams, clientIp(httpRequest), userAgent(httpRequest), deviceId(httpRequest));

        return switch (outcome) {
            case AuthService.LoginOutcome.MfaRequired mfaRequired ->
                    ResponseEntity.ok(new MfaRequiredResponse(mfaRequired.mfaToken()));
            case AuthService.LoginOutcome.ReactivationRequired reactivationRequired ->
                    ResponseEntity.ok(new com.hokyozu.kyofuse.auth.dto.response.ReactivationRequiredResponse(
                            reactivationRequired.reactivationToken(),
                            reactivationRequired.maskedEmail(),
                            reactivationRequired.scheduledDeletion(),
                            reactivationRequired.scheduledDeletionDate()
                    ));
            case AuthService.LoginOutcome.Authenticated authenticated -> {
                applyAuthCookies(response, authenticated.result());
                yield ResponseEntity.ok(AuthMapper.toResponse(authenticated.result().user(), authenticated.result().switchToken()));
            }
        };
    }

    @PostMapping("/reactivate/confirm")
    public ResponseEntity<?> confirmReactivation(
            @RequestBody @Valid com.hokyozu.kyofuse.auth.dto.request.ConfirmReactivationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        User user = accountReactivationService.confirmReactivation(request.reactivationToken(), request.code());
        if (user.isTotpEnabled()) {
            return ResponseEntity.ok(new MfaRequiredResponse(mfaTokenService.generate(user)));
        }
        AuthService.AuthResult result = authService.issueTokens(user, deviceId(httpRequest));
        applyAuthCookies(response, result);
        authService.publishLoginSuccess(user, clientIp(httpRequest), userAgent(httpRequest));
        return ResponseEntity.ok(AuthMapper.toResponse(result.user(), result.switchToken()));
    }

    @PostMapping("/reactivate/resend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendReactivationCode(
            @RequestBody @Valid com.hokyozu.kyofuse.auth.dto.request.ResendReactivationCodeRequest request
    ) {
        accountReactivationService.resendReactivationCode(request.reactivationToken());
    }

    @RateLimit(key = "2fa_verify", limit = 5, period = 300, type = RateLimitType.IP)
    @PostMapping("/2fa/verify")
    public AuthResponse verifyMfa(
            @RequestBody @Valid MfaVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.verifyMfa(request.mfaToken(), request.code(), clientIp(httpRequest), userAgent(httpRequest), deviceId(httpRequest));
        applyAuthCookies(response, result);

        return AuthMapper.toResponse(result.user(), result.switchToken());
    }

    @PostMapping("/switch-account")
    public ResponseEntity<SwitchAccountResponse> switchAccount(
            @RequestBody @Valid SwitchAccountRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.switchAccount(request, clientIp(httpRequest), userAgent(httpRequest));
        applyAuthCookies(response, result);

        return ResponseEntity.ok(AuthMapper.toSwitchResponse(result.user(), result.switchToken()));
    }

    @PostMapping("/disconnect-account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnectAccount(
            @RequestBody @Valid DisconnectAccountRequest request
    ) {
        authService.disconnectAccount(request);
    }

    @PostMapping("/switch-token")
    public ResponseEntity<Map<String, String>> getSwitchToken(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest
    ) {
        String deviceId = deviceId(httpRequest);
        if (deviceId == null || deviceId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        String switchToken = authService.generateSwitchToken(userId, deviceId);
        return ResponseEntity.ok(Map.of("switchToken", switchToken));
    }

    @PostMapping("/2fa/setup")
    public TotpSetupResponse setupMfa(@AuthenticationPrincipal Jwt jwt) {
        return twoFactorAuthService.setup(UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/2fa/confirm")
    public TotpConfirmResponse confirmMfa(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid TotpConfirmRequest request
    ) {
        return new TotpConfirmResponse(twoFactorAuthService.confirm(UUID.fromString(jwt.getSubject()), request.code()));
    }

    @PostMapping("/2fa/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableMfa(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid Disable2faRequest request
    ) {
        twoFactorAuthService.disable(UUID.fromString(jwt.getSubject()), request.password());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = AuthCookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.refresh(refreshToken);
        applyAuthCookies(response, result);

        return AuthMapper.toResponse(result.user(), result.switchToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = AuthCookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken, jwt);
        clearAuthCookies(response);
    }

    public void logout(String refreshToken, HttpServletResponse response) {
        authService.logout(refreshToken);
        clearAuthCookies(response);
    }

    @GetMapping("/me")
    public AuthMeResponse me(@AuthenticationPrincipal Jwt jwt, HttpServletResponse response) {
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hokyozu.kyofuse.shared.exception.UnauthorizedException("Usuário não encontrado."));

        if (user.getStatus() == com.hokyozu.kyofuse.users.enums.UserStatus.INACTIVE) {
            clearAuthCookies(response);
            throw new com.hokyozu.kyofuse.shared.exception.UnauthorizedException("Conta inativa.");
        }

        String profileSetupStatus = gamerProfileRepository.findByUserId(userId)
                .map(profile -> profile.getSetupStatus().name())
                .orElse("PENDING");

        return new AuthMeResponse(
                userId,
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                user.isTotpEnabled(),
                profileSetupStatus
        );
    }

    @RateLimit(key = "forgot_password", limit = 3, period = 900, type = RateLimitType.IP)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@RequestBody @Valid ForgotPasswordRequest request, HttpServletRequest httpRequest) {
        passwordResetService.requestPasswordReset(request.emailOrUsername(), clientIp(httpRequest));
    }

    @GetMapping("/reset-password/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateResetToken(@RequestParam String token) {
        passwordResetService.validateToken(token);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }

    private String clientIp(HttpServletRequest request) {
        return clientIpResolver.resolve(request);
    }

    private String userAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private String deviceId(HttpServletRequest request) {
        return request != null ? request.getHeader("X-Device-Id") : null;
    }

    private void applyAuthCookies(HttpServletResponse response, AuthService.AuthResult result) {
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.buildAccessTokenCookie(result.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.buildRefreshTokenCookie(result.refreshToken()).toString());
    }

    private void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.buildExpiredAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, authCookieService.buildExpiredRefreshTokenCookie().toString());
    }
}
