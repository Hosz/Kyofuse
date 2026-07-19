package com.hokyozu.kyofuse.relationships.friendship.service;

import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendRequestResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendRequest;
import com.hokyozu.kyofuse.relationships.friendship.mapper.UserFriendRequestMapper;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendRequestRepository;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.FriendRequestPermission;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
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
public class UserFriendRequestService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final UserBlockRepository userBlockRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserFriendRequestRepository userFriendRequestRepository;

    @Transactional
    public UserFriendRequestResponse sendRequest(UUID userId, UUID userFriendRequestId) {
        User user = userFinder.findProfileByUserId(userId);
        User userFriendRequest = userFinder.findProfileByUserId(userFriendRequestId);
        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(userFriendRequest);

        userChecker.checkActive(user);
        userChecker.checkActive(userFriendRequest);

        if (userBlockRepository.existsByBlockerAndBlocked(user, userFriendRequest) || userBlockRepository.existsByBlockerAndBlocked(userFriendRequest, user)) {
            throw new IllegalArgumentException("Cannot send friend request to a blocked user.");
        }

        if (settings.getProfileVisibility().equals(ProfileVisibility.PRIVATE)) {
            if (settings.getFriendRequestPermission().equals(FriendRequestPermission.EVERYONE)) {
                if (userFriendRequestRepository.existsBySenderAndReceiver(user, userFriendRequest) ||
                    userFriendRequestRepository.existsBySenderAndReceiver(userFriendRequest, user)) {
                    throw new IllegalArgumentException("Friend request already sent.");
                }
                UserFriendRequest friendRequest = UserFriendRequestMapper.sendRequest(user, userFriendRequest);
                userFriendRequestRepository.save(friendRequest);
                return UserFriendRequestMapper.toResponse(friendRequest);
            } else if (settings.getFriendRequestPermission().equals(FriendRequestPermission.NOBODY)) {
                throw new ForbiddenException("User does not accept friend requests.");
            }
        }

        UserFriendRequest friendRequest = UserFriendRequestMapper.sendRequest(user, userFriendRequest);
        userFriendRequestRepository.save(friendRequest);
        return UserFriendRequestMapper.toResponse(friendRequest);
    }

    @Transactional(readOnly = true)
    public Page<UserFriendRequestResponse> showRequests(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFriendRequest> requests = userFriendRequestRepository.findAllByReceiver(user, pageable);
        return requests.map(UserFriendRequestMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserFriendRequestResponse> showSentRequests(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFriendRequest> requests = userFriendRequestRepository.findAllBySender(user, pageable);
        return requests.map(UserFriendRequestMapper::toResponse);
    }

    @Transactional
    public void removeRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        UserFriendRequest friendRequest = userFriendRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Friend request not found."));
        userChecker.checkActive(user);

        if (!friendRequest.getSender().equals(user) && !friendRequest.getReceiver().equals(user)) {
            throw new ForbiddenException("You are not the owner of this friend request.");
        }

        userFriendRequestRepository.delete(friendRequest);
    }
}
