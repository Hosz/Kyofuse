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
        assertThatCode(() -> validator.validate("email-index-hash", " player "))
                .doesNotThrowAnyException();

        verify(userRepository).existsByEmailIndexAndEmailVerifiedTrue("email-index-hash");
        verify(userRepository).existsByUsernameIgnoreCaseAndEmailVerifiedTrue("player");
    }

    @Test
    void validateThrowsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmailIndexAndEmailVerifiedTrue("email-index-hash")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate("email-index-hash", "player"))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email já está em uso.");
    }

    @Test
    void validateThrowsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsernameIgnoreCaseAndEmailVerifiedTrue("player")).thenReturn(true);

        assertThatThrownBy(() -> validator.validate("email-index-hash", " player "))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username já está em uso.");
    }
}
