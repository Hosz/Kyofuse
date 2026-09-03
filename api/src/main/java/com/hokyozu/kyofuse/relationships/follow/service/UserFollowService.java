package com.hokyozu.kyofuse.relationships.follow.service;

import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.profiles.dto.response.GamerProfileResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.profiles.finder.GamerProfileFinder;
import com.hokyozu.kyofuse.profiles.mapper.GamerProfileMapper;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileFavoriteMapRepository;
import com.hokyozu.kyofuse.profiles.repository.GamerProfileRepository;
import com.hokyozu.kyofuse.relationships.block.repository.UserBlockRepository;
import com.hokyozu.kyofuse.relationships.follow.dto.response.UserFollowResponse;
import com.hokyozu.kyofuse.relationships.follow.entity.UserFollow;
import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.mapper.UserFollowMapper;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.permission.service.follow.FollowPermissionService;
import com.hokyozu.kyofuse.relationships.permission.service.profile.ProfilePermissionService;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final GamerProfileRepository gamerProfileRepository;
    private final GamerProfileFavoriteMapRepository gamerProfileFavoriteMapRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;

    @Transactional
    public UserFollowResponse followUser(UUID userId, UUID userFollowId) {
        User user = userFinder.findProfileByUserId(userId);
        User followedUser = userFinder.findProfileByUserId(userFollowId);

        userChecker.checkActive(user);
        userChecker.checkActive(followedUser);

        followPermissionService.validateSendFollow(user, followedUser);

        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(followedUser);
        boolean isPrivate = settings != null && settings.getProfileVisibility() == ProfileVisibility.PRIVATE;

        UserFollow userFollow = isPrivate
                ? UserFollowMapper.toInvite(user, followedUser)
                : UserFollowMapper.toFollow(user, followedUser);

        userFollowRepository.save(userFollow);

        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(followedUser)
                        .actor(user)
                        .type(isPrivate ? NotificationType.FOLLOW_REQUEST_RECEIVED : NotificationType.FOLLOW_STARTED)
                        .title(isPrivate ? "Solicitação de seguimento." : "Novo seguidor.")
                        .message(isPrivate
                                ? user.getUsername() + " solicitou para te seguir."
                                : user.getUsername() + " começou a te seguir.")
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
        List<UUID> followerIds = userFollow.getContent().stream()
                .map(f -> f.getFollower().getId())
                .distinct()
                .toList();

        Map<UUID, GamerProfile> profileMap = followerIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(followerIds).stream()
                        .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        return userFollow.map(follow -> {
            GamerProfile followerProfile = profileMap.get(follow.getFollower().getId());
            return UserFollowMapper.toResponse(follow, followerProfile);
        });
    }

    @Transactional(readOnly = true)
    public Page<UserFollowResponse> showMyFollowers(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<UserFollow> userFollows = userFollowRepository.findAllByFollowed(user, pageable);
        List<UUID> followerIds = userFollows.getContent().stream()
                .map(f -> f.getFollower().getId())
                .distinct()
                .toList();

        Map<UUID, GamerProfile> profileMap = followerIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(followerIds).stream()
                        .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        return userFollows.map(follow -> {
            GamerProfile followerProfile = profileMap.get(follow.getFollower().getId());
            return UserFollowMapper.toResponse(follow, followerProfile);
        });
    }

    /**
     * Se o usuário logado já segue alguém. O front precisa disso pra mostrar "Seguindo"
     * em vez de "Seguir" — sem essa informação o botão nascia sempre como "Seguir" e o
     * clique batia num follow que o backend recusa.
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(UUID userId, UUID otherUserId) {
        User user = userFinder.findProfileByUserId(userId);
        User other = userFinder.findProfileByUserId(otherUserId);

        return userFollowRepository.existsByFollowerAndFollowed(user, other);
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
        List<UUID> followedIds = userFollows.getContent().stream()
                .map(f -> f.getFollowed().getId())
                .distinct()
                .toList();

        Map<UUID, GamerProfile> profileMap = followedIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(followedIds).stream()
                        .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        return userFollows.map(follow -> {
            GamerProfile followedProfile = profileMap.get(follow.getFollowed().getId());
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
        List<UUID> followedIds = userFollows.getContent().stream()
                .map(f -> f.getFollowed().getId())
                .distinct()
                .toList();

        Map<UUID, GamerProfile> profileMap = followedIds.isEmpty()
                ? Map.of()
                : gamerProfileFinder.findAllByUserIds(followedIds).stream()
                        .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity(), (a, b) -> a));

        return userFollows.map(follow -> {
            GamerProfile followedProfile = profileMap.get(follow.getFollowed().getId());
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

    @Transactional(readOnly = true)
    public Page<GamerProfileResponse> getFollowSuggestions(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Set<UUID> excludedUserIds = new HashSet<>();
        excludedUserIds.add(userId);
        excludedUserIds.addAll(userFollowRepository.findFollowedIdsByFollower(user));
        excludedUserIds.addAll(userBlockRepository.findBlockedIdsByBlocker(user));
        excludedUserIds.addAll(userBlockRepository.findBlockerIdsByBlocked(user));

        Page<GamerProfile> profiles = gamerProfileRepository.findSuggestions(
                UserStatus.ACTIVE, excludedUserIds, pageable);

        List<UUID> profileIds = profiles.stream().map(GamerProfile::getId).toList();
        List<GamerProfileFavoriteMap> maps = profileIds.isEmpty()
                ? List.of()
                : gamerProfileFavoriteMapRepository.findByProfile_IdIn(profileIds);

        Map<UUID, List<GamerProfileFavoriteMap>> mapsByProfile = maps.stream()
                .collect(Collectors.groupingBy(map -> map.getProfile().getId()));

        return profiles.map(profile -> GamerProfileMapper.toResponse(
                profile,
                mapsByProfile.getOrDefault(profile.getId(), List.of())
        ));
    }
}
