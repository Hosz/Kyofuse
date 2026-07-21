package com.hokyozu.kyofuse.relationships.friendship.service;

import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendshipResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendRequest;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendship;
import com.hokyozu.kyofuse.relationships.friendship.mapper.UserFriendshipMapper;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendRequestRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.permission.service.friendship.FriendshipPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
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

    private final ProfilePermissionService profilePermissionService;

    private final UserFriendRequestRepository userFriendRequestRepository;
    private final UserFriendshipRepository userFriendshipRepository;
    private final UserBlockRepository userBlockRepository;
    private final FriendshipPermissionService friendshipPermissionService;

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

        if (!userTwo.equals(user)) {
            throw new ForbiddenException("User does not have permission to accept this friend request.");
        }

        friendshipPermissionService.validateAcceptFriendRequest(userOne, userTwo);

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

        friendshipPermissionService.validateRejectFriendRequest(user, friendRequest.getSender());

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
        userChecker.checkActive(userAuth);
        userChecker.checkActive(user);

        profilePermissionService.validateViewFriends(userAuth, user);

        Page<UserFriendship> friends = userFriendshipRepository.findAllByUserOneOrUserTwo(user, user, pageable);
        return friends.map(UserFriendshipMapper::toResponse);
    }

    @Transactional
    public void removeFriendship(UUID userId, UUID friendId) {
        User user = userFinder.findProfileByUserId(userId);
        User friend = userFinder.findProfileByUserId(friendId);

        userChecker.checkActive(user);
        userChecker.checkActive(friend);

        friendshipPermissionService.validateRemoveFriendship(user, friend);
        UserFriendship friendship = userFriendshipRepository.findByUserOneAndUserTwo(user, friend);

        userFriendshipRepository.delete(friendship);
    }
}
