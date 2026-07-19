package com.hokyozu.kyofuse.relationships.friendship.service;

import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendshipResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendRequest;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;
import com.hokyozu.kyofuse.relationships.friendship.mapper.UserFriendshipMapper;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendRequestRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
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
public class UserFriendshipService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final UserFriendRequestRepository userFriendRequestRepository;
    private final UserFriendshipRepository userFriendshipRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserBlockRepository userBlockRepository;

    @Transactional
    public UserFriendshipResponse acceptRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        UserFriendRequest friendRequest = userFriendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Friend request not found"));
        User userOne = userFinder.findProfileByUserId(friendRequest.getSender().getId());
        User userTwo = userFinder.findProfileByUserId(friendRequest.getReceiver().getId());

        userChecker.checkActive(user);
        userChecker.checkActive(userOne);
        userChecker.checkActive(userTwo);

        if (userBlockRepository.existsByBlockerAndBlocked(userOne, userTwo) ||
                userBlockRepository.existsByBlockerAndBlocked(userTwo, userOne)) {
            throw new ForbiddenException("Cannot accept friend request due to blocking.");
        }

        if (!user.equals(friendRequest.getReceiver()) || !user.equals(friendRequest.getSender())) {
            throw new IllegalArgumentException("User is not authorized to accept this friend request");
        }

        UserFriendship friendship = UserFriendshipMapper.toEntity(userOne, userTwo);
        userFriendshipRepository.save(friendship);
        userFriendRequestRepository.delete(friendRequest);
        return UserFriendshipResponse.toResponse(friendship);
    }

    @Transactional
    public void declineRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        UserFriendRequest friendRequest = userFriendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Friend request not found"));

        userChecker.checkActive(user);

        if (!user.equals(friendRequest.getReceiver())) {
            throw new ForbiddenException("User is not authorized to decline this friend request");
        }

        userFriendRequestRepository.delete(friendRequest);
    }

    @Transactional
    public void cancelRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        UserFriendRequest friendRequest = userFriendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Friend request not found"));

        userChecker.checkActive(user);

        if (!user.equals(friendRequest.getSender())) {
            throw new ForbiddenException("User is not authorized to cancel this friend request");
        }

        userFriendRequestRepository.delete(friendRequest);
    }

    @Transactional
    public Page<UserFriendshipResponse> showMyFriends(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFriendship> friends = userFriendshipRepository.findAllByUserOneOrUserTwo(user, user, pageable);
        return friends.map(UserFriendshipMapper::toResponse);
    }

    @Transactional
    public Page<UserFriendshipResponse> showUserFriends(UUID userAuthId, UUID userId, Pageable pageable) {
        User userAuth = userFinder.findProfileByUserId(userAuthId);
        User user = userFinder.findProfileByUserId(userId);
        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(user);
        userChecker.checkActive(userAuth);
        userChecker.checkActive(user);

        if (userBlockRepository.existsByBlockerAndBlocked(user, userAuth)) {
            throw new ForbiddenException("User is blocked by the target user.");
        }
        if (userBlockRepository.existsByBlockerAndBlocked(userAuth, user)) {
            throw new ForbiddenException("Target user is blocked by the authenticated user.");
        }

        if (settings.getProfileVisibility().equals(ProfileVisibility.PRIVATE)) {
            throw new ForbiddenException("User's friends list is private.");
        }

        Page<UserFriendship> friends = userFriendshipRepository.findAllByUserOneOrUserTwo(user, user, pageable);
        return friends.map(UserFriendshipMapper::toResponse);
    }
}
