package com.hokyozu.kyofuse.relationships.follow.service;

import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.follow.dto.response.UserFollowResponse;
import com.hokyozu.kyofuse.relationships.follow.entity.UserFollow;
import com.hokyozu.kyofuse.relationships.follow.mapper.UserFollowMapper;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
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
    private final UserBlockRepository userBlockRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final UserFollowRepository userFollowRepository;
    private final NotificationService notificationService;

    @Transactional
    public UserFollowResponse followUser(UUID userId, UUID userFollowId) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userFollowId);
        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(followedUser);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        if (userFollowRepository.existsByFollowerAndFollowed(user, followedUser)) {
            throw new ForbiddenException("You are already following this user.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(followedUser, user)) {
            throw new ForbiddenException("You cannot follow this user because they have blocked you.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(user, followedUser)) {
            throw new ForbiddenException("You cannot follow this user because you have blocked them.");
        }

        if (settings.getProfileVisibility().equals(ProfileVisibility.PRIVATE)) {
            if (settings.getFollowPermission().equals(FollowPermission.APPROVAL_REQUIRED)) {
                UserFollow userFollow = UserFollowMapper.toInvite(user, followedUser);
                userFollowRepository.save(userFollow);
                notificationService.createNotification(
                        CreateNotificationRequest.builder()
                                .recipient(followedUser)
                                .actor(user)
                                .type(NotificationType.FOLLOW_REQUEST_RECEIVED)
                                .title("Solicitação para seguir recebida.")
                                .message(user.getUsername() + " solicitou seguir você.")
                                .targetType(NotificationTargetType.FOLLOW)
                                .targetId(followedUser.getId())
                                .metadata(Map.of(
                                        "Solicitação para seguir de: ", followedUser.getUsername()
                                ))
                                .build()
                );
                return UserFollowMapper.toResponse(userFollow);
            }
        }

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
        return UserFollowMapper.toResponse(userFollow);
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showFollowers(UUID userId, UUID userIdFollowers, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userIdFollowers);
        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(followedUser);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        if (userBlockRepository.existsByBlockerAndBlocked(followedUser, user)) {
            throw new ForbiddenException("You cannot view this user's followers because they have blocked you.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(user, followedUser)) {
            throw new ForbiddenException("You cannot view this user's followers because you have blocked them.");
        }

        if (settings.getProfileVisibility().equals(ProfileVisibility.PRIVATE)) {
            if (settings.getFollowersVisibility().equals(ProfileVisibility.PRIVATE)) {
                throw new ForbiddenException("You cannot view this user's followers because their profile is private.");
            } else if (settings.getFollowersVisibility().equals(ProfileVisibility.PUBLIC)) {
                Page<UserFollow> userFollows = userFollowRepository.findAllByFollowed(user, pageable);
                return userFollows.map(UserFollowMapper::toResponse);
            }
        }

        Page<UserFollow> userFollow = userFollowRepository.findAllByFollowed(user, pageable);
        return userFollow.map(UserFollowMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showMyFollowers(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollowed(user, pageable);
        return userFollows.map(UserFollowMapper::toResponse);
    }

    @Transactional
    public void unfollowUser(UUID userId, UUID followingId) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(followingId);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        if (userBlockRepository.existsByBlockerAndBlocked(followedUser, user)) {
            throw new ForbiddenException("You cannot unfollow this user because they have blocked you.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(user, followedUser)) {
            throw new ForbiddenException("You cannot unfollow this user because you have blocked them.");
        }

        if (!userFollowRepository.existsByFollowerAndFollowed(user, followedUser)) {
            throw new NotFoundException("You are not following this user.");
        }

        UserFollow userFollow = userFollowRepository.findByFollowerAndFollowed(user, followedUser);
        if (!userFollow.getFollower().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to unfollow this user.");
        }
        userFollowRepository.delete(userFollow);
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showMyFollowings(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollower(user, pageable);
        return userFollows.map(UserFollowMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showFollowings(UUID userId, UUID userIdFollowing, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userIdFollowing);
        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(followedUser);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        if (userBlockRepository.existsByBlockerAndBlocked(followedUser, user)) {
            throw new ForbiddenException("You cannot view this user's followings because they have blocked you.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(user, followedUser)) {
            throw new ForbiddenException("You cannot view this user's followings because you have blocked them.");
        }

        if (settings.getProfileVisibility().equals(ProfileVisibility.PRIVATE)) {
            if (settings.getFollowingVisibility().equals(ProfileVisibility.PRIVATE)) {
                throw new ForbiddenException("You cannot view this user's followings because their profile is private.");
            } else if (settings.getFollowingVisibility().equals(ProfileVisibility.PUBLIC)) {
                Page<UserFollow> userFollows = userFollowRepository.findAllByFollower(user, pageable);
                return userFollows.map(UserFollowMapper::toResponse);
            }
        }

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollower(user, pageable);
        return userFollows.map(UserFollowMapper::toResponse);
    }

    @Transactional
    public Page<UserFollowResponse> deleteFollowing(UUID userId, UUID userIdFollowing) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userIdFollowing);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        if (userBlockRepository.existsByBlockerAndBlocked(followedUser, user)) {
            throw new ForbiddenException("You cannot delete this following because they have blocked you.");
        }

        if (userBlockRepository.existsByBlockerAndBlocked(user, followedUser)) {
            throw new ForbiddenException("You cannot delete this following because you have blocked them.");
        }

        if (!userFollowRepository.existsByFollowerAndFollowed(user, followedUser)) {
            throw new NotFoundException("You are not following this user.");
        }

        UserFollow userFollow = userFollowRepository.findByFollowerAndFollowed(user, followedUser);

        if (!userFollow.getFollowed().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to delete this following.");
        }
        userFollowRepository.delete(userFollow);

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollower(user, Pageable.unpaged());
        return userFollows.map(UserFollowMapper::toResponse);
    }
}
