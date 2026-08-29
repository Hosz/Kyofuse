package com.hokyozu.kyofuse.auth.controller;

import com.hokyozu.kyofuse.auth.dto.request.*;
import com.hokyozu.kyofuse.auth.dto.response.*;
import com.hokyozu.kyofuse.auth.mapper.AuthMapper;
import com.hokyozu.kyofuse.auth.service.AuthService;
import com.hokyozu.kyofuse.auth.service.EmailVerificationService;
import com.hokyozu.kyofuse.auth.service.PasswordResetService;
import com.hokyozu.kyofuse.auth.service.TwoFactorAuthService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
import com.hokyozu.kyofuse.infrastructure.security.steam.SteamService;
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

        return AuthMapper.toResponse(result.user());
    }

    @GetMapping("/verify-email/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateEmailVerificationToken(@RequestParam String token) {
        emailVerificationService.validateToken(token);
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public void resendVerification(
            @RequestBody @Valid ResendVerificationEmailRequest request,
            HttpServletRequest httpRequest
    ) {
        emailVerificationService.resendVerification(request.emailOrUsername(), clientIp(httpRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.LoginOutcome outcome = authService.login(request, clientIp(httpRequest));

        return switch (outcome) {
            case AuthService.LoginOutcome.MfaRequired mfaRequired ->
                    ResponseEntity.ok(new MfaRequiredResponse(mfaRequired.mfaToken()));
            case AuthService.LoginOutcome.Authenticated authenticated -> {
                applyAuthCookies(response, authenticated.result());
                yield ResponseEntity.ok(AuthMapper.toResponse(authenticated.result().user()));
            }
        };
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(
            @RequestBody @Valid GoogleLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.LoginOutcome outcome = authService.loginWithGoogle(request.idToken(), clientIp(httpRequest));

        return switch (outcome) {
            case AuthService.LoginOutcome.MfaRequired mfaRequired ->
                    ResponseEntity.ok(new MfaRequiredResponse(mfaRequired.mfaToken()));
            case AuthService.LoginOutcome.Authenticated authenticated -> {
                applyAuthCookies(response, authenticated.result());
                yield ResponseEntity.ok(AuthMapper.toResponse(authenticated.result().user()));
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
        AuthService.LoginOutcome outcome = authService.loginWithSteam(openIdParams, clientIp(httpRequest));

        return switch (outcome) {
            case AuthService.LoginOutcome.MfaRequired mfaRequired ->
                    ResponseEntity.ok(new MfaRequiredResponse(mfaRequired.mfaToken()));
            case AuthService.LoginOutcome.Authenticated authenticated -> {
                applyAuthCookies(response, authenticated.result());
                yield ResponseEntity.ok(AuthMapper.toResponse(authenticated.result().user()));
            }
        };
    }

    @PostMapping("/2fa/verify")
    public AuthResponse verifyMfa(
            @RequestBody @Valid MfaVerifyRequest request,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.verifyMfa(request.mfaToken(), request.code());
        applyAuthCookies(response, result);

        return AuthMapper.toResponse(result.user());
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

        return AuthMapper.toResponse(result.user());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(name = AuthCookieService.REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        clearAuthCookies(response);
    }

    @GetMapping("/me")
    public AuthMeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return new AuthMeResponse(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("username"),
                jwt.getClaimAsString("role"),
                Boolean.TRUE.equals(jwt.getClaim("totpEnabled"))
        );
    }

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
        return request.getRemoteAddr();
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
