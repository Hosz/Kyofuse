package com.hokyozu.kyofuse.auth.validator;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EmailCipherService;
import com.hokyozu.kyofuse.shared.exception.UnauthorizedException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginFinderValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailCipherService emailCipherService;

    @InjectMocks
    private LoginFinderValidator validator;

    @Test
    void validateFindsUserByEmailFirst() {
        User user = User.builder().email("user@example.com").build();
        when(emailCipherService.blindIndex("user@example.com")).thenReturn("email-index-hash");
        when(userRepository.findByEmailIndex("email-index-hash")).thenReturn(Optional.of(user));

        User result = validator.validate(new LoginRequest(" User@Example.com ", "password123"));

        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmailIndex("email-index-hash");
    }

    @Test
    void validateFindsUserByUsernameWhenEmailDoesNotMatch() {
        User user = User.builder().username("player").build();
        when(emailCipherService.blindIndex("player")).thenReturn("email-index-hash");
        when(userRepository.findByEmailIndex("email-index-hash")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("player")).thenReturn(Optional.of(user));

        User result = validator.validate(new LoginRequest(" PLAYER ", "password123"));

        assertThat(result).isSameAs(user);
    }

    @Test
    void validateThrowsWhenUserIsNotFound() {
        when(emailCipherService.blindIndex("missing")).thenReturn("email-index-hash");
        when(userRepository.findByEmailIndex("email-index-hash")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validate(new LoginRequest("missing", "password123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas.");
    }
}
