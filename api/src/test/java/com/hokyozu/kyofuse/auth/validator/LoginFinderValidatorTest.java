package com.hokyozu.kyofuse.auth.validator;

import com.hokyozu.kyofuse.auth.dto.request.LoginRequest;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
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

    @InjectMocks
    private LoginFinderValidator validator;

    @Test
    void validateFindsUserByEmailFirst() {
        User user = User.builder().email("user@example.com").build();
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        User result = validator.validate(new LoginRequest(" User@Example.com ", "password123"));

        assertThat(result).isSameAs(user);
        verify(userRepository).findByEmailIgnoreCase("user@example.com");
    }

    @Test
    void validateFindsUserByUsernameWhenEmailDoesNotMatch() {
        User user = User.builder().username("player").build();
        when(userRepository.findByEmailIgnoreCase("player")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("player")).thenReturn(Optional.of(user));

        User result = validator.validate(new LoginRequest(" PLAYER ", "password123"));

        assertThat(result).isSameAs(user);
    }

    @Test
    void validateThrowsWhenUserIsNotFound() {
        when(userRepository.findByEmailIgnoreCase("missing")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validator.validate(new LoginRequest("missing", "password123")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas.");
    }
}
