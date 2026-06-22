package com.hokyozu.kyofuse.users.finder;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFinderTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFinder userFinder;

    @Test
    void findProfileByUserIdReturnsUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userFinder.findProfileByUserId(userId);

        assertThat(result).isSameAs(user);
    }

    @Test
    void findProfileByUserIdRejectsMissingUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userFinder.findProfileByUserId(userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found for ID: " + userId);
    }
}
