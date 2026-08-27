package com.hokyozu.kyofuse.auth.controller;

import com.hokyozu.kyofuse.auth.dto.request.Disable2faRequest;
import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.dto.request.MfaVerifyRequest;
import com.hokyozu.kyofuse.auth.dto.request.RegisterRequest;
import com.hokyozu.kyofuse.auth.dto.request.TotpConfirmRequest;
import com.hokyozu.kyofuse.auth.dto.response.AuthMeResponse;
import com.hokyozu.kyofuse.auth.dto.response.AuthResponse;
import com.hokyozu.kyofuse.auth.dto.response.MfaRequiredResponse;
import com.hokyozu.kyofuse.auth.dto.response.TotpConfirmResponse;
import com.hokyozu.kyofuse.auth.dto.response.TotpSetupResponse;
import com.hokyozu.kyofuse.auth.mapper.AuthMapper;
import com.hokyozu.kyofuse.auth.service.AuthService;
import com.hokyozu.kyofuse.auth.service.TwoFactorAuthService;
import com.hokyozu.kyofuse.infrastructure.security.jwt.AuthCookieService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final AuthCookieService authCookieService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        AuthService.AuthResult result = authService.register(request, clientIp(httpRequest));
        applyAuthCookies(response, result);

        return AuthMapper.toResponse(result.user());
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

    /**
     * Sem proxy reverso na frente hoje, então o IP de origem é o do socket direto. Se um
     * dia entrar um load balancer/reverse proxy, isso precisa virar uma resolução de
     * X-Forwarded-For restrita a proxies confiáveis — confiar nesse header sem validação
     * permite qualquer cliente forjar o IP e burlar o limitador por completo.
     */
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
