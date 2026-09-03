package com.hokyozu.kyofuse.relationships.friendship.service;

import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFriendshipService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;

    private final ProfilePermissionService profilePermissionService;

    private final UserFriendRequestRepository userFriendRequestRepository;
    private final UserFriendshipRepository userFriendshipRepository;
    private final FriendshipPermissionService friendshipPermissionService;
    private final GamerProfileFinder gamerProfileFinder;

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
        User friend = UserFriendshipMapper.resolveFriend(friendship, userId);
        GamerProfile gamerProfile = gamerProfileFinder.findProfileByUserId(friend.getId());
        return UserFriendshipMapper.toResponse(friendship, userId, gamerProfile);
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

    @Transactional(readOnly = true)
    public Page<UserFriendshipResponse> showMyFriends(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFriendship> friends = userFriendshipRepository.findAllByUserOneOrUserTwo(user, user, pageable);
        List<UUID> friendIds = friends.getContent().stream()
                .map(f -> UserFriendshipMapper.resolveFriend(f, userId).getId())
                .distinct()
                .toList();

        Map<UUID, GamerProfile> profileMap = friendIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(friendIds).stream()
                        .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        return friends.map(friendship -> {
            User friend = UserFriendshipMapper.resolveFriend(friendship, userId);
            GamerProfile gamerProfile = profileMap.get(friend.getId());
            return UserFriendshipMapper.toResponse(friendship, userId, gamerProfile);
        });
    }

    @Transactional(readOnly = true)
    public Page<UserFriendshipResponse> showUserFriends(UUID userAuthId, UUID userId, Pageable pageable) {
        User userAuth = userFinder.findProfileByUserId(userAuthId);
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(userAuth);
        userChecker.checkActive(user);

        profilePermissionService.validateViewFriends(userAuth, user);

        Page<UserFriendship> friends = userFriendshipRepository.findAllByUserOneOrUserTwo(user, user, pageable);
        List<UUID> friendIds = friends.getContent().stream()
                .map(f -> UserFriendshipMapper.resolveFriend(f, userId).getId())
                .distinct()
                .toList();

        Map<UUID, GamerProfile> profileMap = friendIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(friendIds).stream()
                        .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        return friends.map(friendship -> {
            User friend = UserFriendshipMapper.resolveFriend(friendship, userId);
            GamerProfile gamerProfile = profileMap.get(friend.getId());
            return UserFriendshipMapper.toResponse(friendship, userId, gamerProfile);
        });
    }

    @Transactional
    public void removeFriendship(UUID userId, UUID friendId) {
        User user = userFinder.findProfileByUserId(userId);
        User friend = userFinder.findProfileByUserId(friendId);

        userChecker.checkActive(user);
        userChecker.checkActive(friend);

        friendshipPermissionService.validateRemoveFriendship(user, friend);
        UserFriendship friendship = userFriendshipRepository.findFriendshipBetween(user, friend)
                .orElseThrow(() -> new NotFoundException("Friendship not found"));

        userFriendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public long showMyFriendsQuantity(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        return userFriendshipRepository.countTotalFriends(user);
    }

    @Transactional(readOnly = true)
    public long showUserFriendsQuantity(UUID userAuthId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        return userFriendshipRepository.countTotalFriends(user);
    }

    @Transactional(readOnly = true)
    public boolean isFriend(UUID userAuthId, UUID userId) {
        User userAuth = userFinder.findProfileByUserId(userAuthId);
        User user = userFinder.findProfileByUserId(userId);
        return userFriendshipRepository.existsMutualFriendship(userAuth, user);
    }

    @Transactional(readOnly = true)
    public com.hokyozu.kyofuse.relationships.friendship.dto.response.FriendshipStatusResponse getFriendshipStatus(UUID userAuthId, UUID userId) {
        User userAuth = userFinder.findProfileByUserId(userAuthId);
        User user = userFinder.findProfileByUserId(userId);

        if (userAuth.equals(user)) {
            return new com.hokyozu.kyofuse.relationships.friendship.dto.response.FriendshipStatusResponse(false, false, false, null);
        }

        if (userFriendshipRepository.existsMutualFriendship(userAuth, user)) {
            return new com.hokyozu.kyofuse.relationships.friendship.dto.response.FriendshipStatusResponse(true, false, false, null);
        }

        var sentReq = userFriendRequestRepository.findBySenderAndReceiver(userAuth, user);
        if (sentReq.isPresent()) {
            return new com.hokyozu.kyofuse.relationships.friendship.dto.response.FriendshipStatusResponse(false, true, false, sentReq.get().getId());
        }

        var recvReq = userFriendRequestRepository.findBySenderAndReceiver(user, userAuth);
        if (recvReq.isPresent()) {
            return new com.hokyozu.kyofuse.relationships.friendship.dto.response.FriendshipStatusResponse(false, false, true, recvReq.get().getId());
        }

        return new com.hokyozu.kyofuse.relationships.friendship.dto.response.FriendshipStatusResponse(false, false, false, null);
    }
}
