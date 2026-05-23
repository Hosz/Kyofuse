package com.hokyozu.kyofuse.auth.validator;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailAndUsernameAvailabilityValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailAndUsernameAvailabilityValidator validator;

    @Test
    void validatePassesWhenEmailAndUsernameAreAvailable() {
        assertThatCode(() -> validator.validate(" user@example.com ", " player "))
                .doesNotThrowAnyException();

        verify(userRepository).existsByEmailIgnoreCase("user@example.com");
        verify(userRepository).existsByUsernameIgnoreCase("player");
    }

    @Test
    void validateThrowsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate(" user@example.com ", "player"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email já está em uso.");
    }

    @Test
    void validateThrowsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsernameIgnoreCase("player")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate("user@example.com", " player "))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username já está em uso.");
    }
}
