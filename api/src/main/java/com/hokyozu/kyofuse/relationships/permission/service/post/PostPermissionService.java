package com.hokyozu.kyofuse.relationships.permission.service.post;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.relationships.follow.enums.FollowStatus;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostPermissionService {

    private final BlockValidator blockValidator;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserFriendshipRepository userFriendshipRepository;

    public void validateViewPost(User viewer, Post post) {
        try {
            validatePrivateProfileAccess(viewer, post);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to view this post.");
        }
    }

    public void validateComment(User viewer, Post post) {
        try {
            validatePrivateProfileAccess(viewer, post);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to comment on this post.");
        }
    }

    public void validateReact(User viewer, Post post) {
        try {
            validatePrivateProfileAccess(viewer, post);
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to react to this post.");
        }
    }

    private boolean areTeammates(User first, User second) {
        return teamMemberRepository.areTeammates(first, second);
    }

    private void validatePrivateProfileAccess(User viewer, Post post) {
        blockValidator.validate(viewer, post.getAuthor());

        if (viewer.getId().equals(post.getAuthor().getId())) {
            return;
        }

        if (post.getVisibility().equals(PostVisibility.TEAM_ONLY)) {
            if (areTeammates(viewer, post.getAuthor())) {
                return;
            }
            throw new ForbiddenException("User does not have permission to view this post.");
        }

        if (post.getVisibility().equals(PostVisibility.PRIVATE)) {
            throw new ForbiddenException("User does not have permission to view this post.");
        }

        if (canViewAuthorPosts(viewer, post.getAuthor())) {
            return;
        }

        throw new ForbiddenException("User does not have permission to view this post.");
    }

    public List<User> filterViewableAuthors(User viewer, List<User> authors) {
        if (authors.isEmpty()) {
            return List.of();
        }

        Map<UUID, ProfileVisibility> visibilityByAuthor = userPrivacySettingsRepository.findAllByUserIn(authors)
                .stream()
                .collect(Collectors.toMap(settings -> settings.getUser().getId(), UserPrivacySettings::getProfileVisibility));

        Map<ProfileVisibility, List<User>> authorsByVisibility = new EnumMap<>(ProfileVisibility.class);
        for (User author : authors) {
            ProfileVisibility visibility = visibilityByAuthor.get(author.getId());
            if (visibility != null) {
                authorsByVisibility.computeIfAbsent(visibility, key -> new ArrayList<>()).add(author);
            }
        }

        Set<UUID> viewable = new HashSet<>();

        authorsByVisibility.getOrDefault(ProfileVisibility.PUBLIC, List.of())
                .forEach(author -> viewable.add(author.getId()));

        List<User> followersOnly = authorsByVisibility.getOrDefault(ProfileVisibility.FOLLOWERS, List.of());
        if (!followersOnly.isEmpty()) {
            viewable.addAll(userFollowRepository.findFollowedIdsByFollowerAndFollowedIn(viewer, followersOnly));
        }

        List<User> friendsOnly = authorsByVisibility.getOrDefault(ProfileVisibility.FRIENDS, List.of());
        if (!friendsOnly.isEmpty()) {
            viewable.addAll(userFriendshipRepository.findMutualFriendIdsIn(viewer, friendsOnly));
        }

        List<User> privateOnly = authorsByVisibility.getOrDefault(ProfileVisibility.PRIVATE, List.of());
        if (!privateOnly.isEmpty()) {
            viewable.addAll(userFollowRepository.findFollowedIdsByFollowerAndFollowedIn(viewer, privateOnly));
        }

        return authors.stream()
                .filter(author -> author.getId().equals(viewer.getId()) || viewable.contains(author.getId()))
                .toList();
    }

    public boolean canViewAuthorPosts(User viewer, User author) {
        if (viewer.getId().equals(author.getId())) {
            return true;
        }

        UserPrivacySettings settings = userPrivacySettingsRepository.findByUser(author);
        ProfileVisibility visibility = settings != null ? settings.getProfileVisibility() : ProfileVisibility.PUBLIC;

        return switch (visibility) {
            case PUBLIC -> true;
            case FOLLOWERS, PRIVATE -> userFollowRepository.existsByFollowerAndFollowedAndStatus(viewer, author, FollowStatus.ACTIVE)
                    || userFriendshipRepository.existsMutualFriendship(viewer, author);
            case FRIENDS -> userFriendshipRepository.existsMutualFriendship(viewer, author);
        };
    }
}
