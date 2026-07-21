package com.hokyozu.kyofuse.relationships.friendship.service;

import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.friendship.dto.response.UserFriendRequestResponse;
import com.hokyozu.kyofuse.relationships.friendship.entity.UserFriendRequest;
import com.hokyozu.kyofuse.relationships.friendship.mapper.UserFriendRequestMapper;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendRequestRepository;
import com.hokyozu.kyofuse.relationships.permission.service.friendship.FriendshipPermissionService;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.FriendRequestPermission;
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
public class UserFriendRequestService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final UserFriendRequestRepository userFriendRequestRepository;
    private final FriendshipPermissionService friendshipPermissionService;

    @Transactional
    public UserFriendRequestResponse sendRequest(UUID userId, UUID userFriendRequestId) {
        User user = userFinder.findProfileByUserId(userId);
        User userFriendRequest = userFinder.findProfileByUserId(userFriendRequestId);

        userChecker.checkActive(user);
        userChecker.checkActive(userFriendRequest);

       friendshipPermissionService.validateSendFriendRequest(user, userFriendRequest);

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
        UserFriendRequest friendRequest = userFriendRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Friend request not found."));
        userChecker.checkActive(user);

        friendshipPermissionService.validateCancelFriendRequest(user, friendRequest.getReceiver());

        userFriendRequestRepository.delete(friendRequest);
    }
}
