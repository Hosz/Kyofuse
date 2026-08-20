package com.hokyozu.kyofuse.users.finder;

import com.hokyozu.kyofuse.auth.repository.UserRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@AllArgsConstructor
public class UserFinder {

    private final UserRepository userRepository;

    public User findProfileByUserId(UUID user) {
        return userRepository.findById(user)
                .orElseThrow(() -> new BadRequestException("User not found for ID: " + user));
    }

    /** Busca vários de uma vez; ids sem usuário correspondente são simplesmente omitidos. */
    public List<User> findAllByIds(List<UUID> ids) {
        return ids.isEmpty() ? List.of() : userRepository.findAllById(ids);
    }
}
