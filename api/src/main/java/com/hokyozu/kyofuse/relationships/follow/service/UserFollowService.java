package com.hokyozu.kyofuse.relationships.follow.service;

import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.follow.dto.response.UserFollowResponse;
import com.hokyozu.kyofuse.relationships.follow.entity.UserFollow;
import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.mapper.UserFollowMapper;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.permission.service.follow.FollowPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.FollowPermission;
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

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;

    private final NotificationService notificationService;
    private final ProfilePermissionService profilePermissionService;

    private final UserBlockRepository userBlockRepository;
    private final UserFollowRepository userFollowRepository;
    private final FollowPermissionService followPermissionService;
    private final GamerProfileFinder gamerProfileFinder;

    @Transactional
    public UserFollowResponse followUser(UUID userId, UUID userFollowId) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userFollowId);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        followPermissionService.validateSendFollow(user, followedUser);

        UserFollow userFollow = UserFollowMapper.toFollow(user, followedUser);
        userFollowRepository.save(userFollow);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(followedUser)
                        .actor(user)
                        .type(NotificationType.FOLLOW_STARTED)
                        .title("Novo seguidor.")
                        .message(user.getUsername() + " começou a te seguir.")
                        .targetType(NotificationTargetType.FOLLOW)
                        .targetId(followedUser.getId())
                        .metadata(Map.of(
                                "Followed by: ", followedUser.getUsername()
                        ))
                        .build()
        );
        GamerProfile followedProfile = gamerProfileFinder.findProfileByUserId(followedUser.getId());
        return UserFollowMapper.toResponse(userFollow, followedProfile);
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showFollowers(UUID userId, UUID userIdFollowers, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userIdFollowers);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        profilePermissionService.validateViewFollowers(user, followedUser);

        Page<UserFollow> userFollow = userFollowRepository.findAllByFollowed(followedUser, pageable);
        return userFollow.map(follow -> {
            GamerProfile followerProfile = gamerProfileFinder.findProfileByUserId(follow.getFollower().getId());
            return UserFollowMapper.toResponse(follow, followerProfile);
        });
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showMyFollowers(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollowed(user, pageable);
        return userFollows.map(follow -> {
            GamerProfile followerProfile = gamerProfileFinder.findProfileByUserId(follow.getFollower().getId());
            return UserFollowMapper.toResponse(follow, followerProfile);
        });
    }

    @Transactional
    public void unfollowUser(UUID userId, UUID followingId) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(followingId);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        followPermissionService.validateUnfollow(user, followedUser);

        UserFollow userFollow = userFollowRepository.findByFollowerAndFollowed(user, followedUser);

        userFollowRepository.delete(userFollow);
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showMyFollowings(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollower(user, pageable);
        return userFollows.map(follow -> {
            GamerProfile followedProfile = gamerProfileFinder.findProfileByUserId(follow.getFollowed().getId());
            return UserFollowMapper.toResponse(follow, followedProfile);
        });
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showFollowings(UUID userId, UUID userIdFollowing, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userIdFollowing);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        profilePermissionService.validateViewFollowing(user, followedUser);

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollower(followedUser, pageable);
        return userFollows.map(follow -> {
            GamerProfile followedProfile = gamerProfileFinder.findProfileByUserId(follow.getFollowed().getId());
            return UserFollowMapper.toResponse(follow, followedProfile);
        });
    }

    @Transactional
    public void removeFollower(UUID userId, UUID userIdFollowing) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userIdFollowing);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        followPermissionService.validateRemoveFollower(user, followedUser);

        UserFollow userFollow = userFollowRepository.findByFollowerAndFollowed(user, followedUser);

        userFollowRepository.delete(userFollow);
    }

    @Transactional
    public void rejectFollowRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        User sender = userFinder.findProfileByUserId(requestId);

        userChecker.checkActive(user);
        userChecker.checkActive(sender);

        followPermissionService.validateRejectFollow(user, sender);

        UserFollow request = userFollowRepository.findByFollowerAndFollowedAndStatus(sender, user, FollowStatus.PENDING);
        userFollowRepository.delete(request);
    }

    @Transactional
    public UserFollowResponse acceptFollowRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        User sender = userFinder.findProfileByUserId(requestId);

        userChecker.checkActive(user);
        userChecker.checkActive(sender);

        followPermissionService.validateAcceptFollow(user, sender);

        UserFollow request = userFollowRepository.findByFollowerAndFollowedAndStatus(sender, user, FollowStatus.PENDING);
        request.setStatus(FollowStatus.ACTIVE);
        userFollowRepository.save(request);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(sender)
                        .actor(user)
                        .type(NotificationType.FOLLOW_REQUEST_ACCEPTED)
                        .title("Solicitação de seguimento aceita.")
                        .message(user.getUsername() + " aceitou sua solicitação de seguimento.")
                        .targetType(NotificationTargetType.FOLLOW)
                        .targetId(sender.getId())
                        .metadata(Map.of(
                                "Accepted by: ", user.getUsername()
                        ))
                        .build()
        );

        GamerProfile senderProfile = gamerProfileFinder.findProfileByUserId(sender.getId());
        return UserFollowMapper.toResponse(request, senderProfile);
    }

    @Transactional(readOnly = true)
    public Long showFollowersQuantity(UUID userIdFollowers) {
        User followedUser = userFinder.findProfileByUserId(userIdFollowers);
        return userFollowRepository.countByFollowed(followedUser);
    }

    @Transactional(readOnly = true)
    public Long showMyFollowersQuantity(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        return userFollowRepository.countByFollowed(user);
    }

    @Transactional(readOnly = true)
    public Long showMyFollowingsQuantity(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        return userFollowRepository.countByFollower(user);
    }

    @Transactional(readOnly = true)
    public Long showFollowingQuantity(UUID userIdFollowing) {
        User user = userFinder.findProfileByUserId(userIdFollowing);
        return userFollowRepository.countByFollower(user);
    }
}
