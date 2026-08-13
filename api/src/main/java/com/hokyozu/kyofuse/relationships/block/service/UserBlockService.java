package com.hokyozu.kyofuse.relationships.block.service;

import com.hokyozu.kyofuse.relationships.block.dto.response.UserBlockResponse;
import com.hokyozu.kyofuse.relationships.block.entity.UserBlock;
import com.hokyozu.kyofuse.relationships.block.mapper.UserBlockMapper;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;

    private final UserBlockRepository userBlockRepository;

    @Transactional
    public UserBlockResponse blockUser(UUID userId, UUID userBlockId) {
        User user = userFinder.findProfileByUserId(userId);
        User userBlocked = userFinder.findProfileByUserId(userBlockId);

        userChecker.checkActive(user);
        userChecker.checkActive(userBlocked);

        if (userBlocked.equals(user)) {
            throw new BadRequestException("You cannot block yourself.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(user, userBlocked)) {
            throw new BadRequestException("User is already blocked.");
        } else if (userBlockRepository.existsByBlockedAndBlocker(user, userBlocked)) {
            throw new BadRequestException("User is already blocking you.");
        }

        UserBlock block = UserBlockMapper.toEntity(user, userBlocked);
        userBlockRepository.save(block);

        return UserBlockMapper.toResponse(block);
    }

    @Transactional(readOnly = true)
    public Page<UserBlockResponse> getBlockedUsers(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserBlock> blocks = userBlockRepository.findAllByBlocker(user, pageable);
        return blocks.map(UserBlockMapper::toResponse);
    }

    @Transactional
    public void unblockUser(UUID userId, UUID userBlockId) {
        User user = userFinder.findProfileByUserId(userId);
        User blocked = userFinder.findProfileByUserId(userBlockId);
        userChecker.checkActive(blocked);
        userChecker.checkActive(user);

        if (!userBlockRepository.existsByBlockerAndBlocked(user, blocked)) {
            throw new BadRequestException("User is not blocked.");
        }

        UserBlock block = userBlockRepository.findByBlockerAndBlocked(user, blocked);
        userBlockRepository.delete(block);
    }
}
