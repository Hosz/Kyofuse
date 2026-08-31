package com.hokyozu.kyofuse.users.controller;

import com.hokyozu.kyofuse.auth.dto.request.GoogleLoginRequest;
import com.hokyozu.kyofuse.users.dto.request.ChangePasswordRequest;
import com.hokyozu.kyofuse.users.dto.request.UpdateEmailRequest;
import com.hokyozu.kyofuse.users.dto.request.UpdateUsernameRequest;
import com.hokyozu.kyofuse.users.dto.response.UserAccountResponse;
import com.hokyozu.kyofuse.users.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAccountService userAccountService;

    @GetMapping("/me/account")
    public UserAccountResponse getAccount(@AuthenticationPrincipal Jwt jwt) {
        return userAccountService.getAccount(UUID.fromString(jwt.getSubject()));
    }

    @PatchMapping("/me/username")
    public UserAccountResponse updateUsername(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UpdateUsernameRequest request
    ) {
        return userAccountService.updateUsername(UUID.fromString(jwt.getSubject()), request);
    }

    @PatchMapping("/me/email")
    public UserAccountResponse updateEmail(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid UpdateEmailRequest request
    ) {
        return userAccountService.updateEmail(UUID.fromString(jwt.getSubject()), request);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        userAccountService.changePassword(UUID.fromString(jwt.getSubject()), request);
    }

    @PostMapping("/me/social/google")
    public UserAccountResponse linkGoogle(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GoogleLoginRequest request
    ) {
        return userAccountService.linkGoogle(UUID.fromString(jwt.getSubject()), request.idToken());
    }

    @DeleteMapping("/me/social/google")
    public UserAccountResponse unlinkGoogle(@AuthenticationPrincipal Jwt jwt) {
        return userAccountService.unlinkGoogle(UUID.fromString(jwt.getSubject()));
    }

    @PostMapping("/me/social/steam")
    public UserAccountResponse linkSteam(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> openIdParams
    ) {
        return userAccountService.linkSteam(UUID.fromString(jwt.getSubject()), openIdParams);
    }

    @DeleteMapping("/me/social/steam")
    public UserAccountResponse unlinkSteam(@AuthenticationPrincipal Jwt jwt) {
        return userAccountService.unlinkSteam(UUID.fromString(jwt.getSubject()));
    }
}
