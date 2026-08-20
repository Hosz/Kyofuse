package com.hokyozu.kyofuse.relationships.permission.service.post;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.relationships.follow.repository.UserFollowRepository;
import com.hokyozu.kyofuse.relationships.friendship.repository.UserFriendshipRepository;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.entity.UserPrivacySettings;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
            throw new ForbiddenException("User does not have permission to view this post..");
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

    /**
     * Post PRIVATE é sempre só do autor, e TEAM_ONLY só de quem é do mesmo time —
     * nenhum dos dois passa pela preferência de postsVisibility. Post PUBLIC ainda
     * fica sujeito ao postsVisibility do autor (FOLLOWERS/FRIENDS/PRIVATE), que
     * controla quem enxerga os posts dela de forma geral.
     */
    private void validatePrivateProfileAccess(User viewer, Post post) {

        blockValidator.validate(viewer, post.getAuthor());

        if (viewer.getId().equals(post.getAuthor().getId())) {
            return;
        }

        if (post.getVisibility().equals(PostVisibility.TEAM_ONLY)) {
            if (areTeammates(viewer, post.getAuthor())) {
                return;
            }
            throw new ForbiddenException("User does not have permission to view this post..");
        }

        if (post.getVisibility().equals(PostVisibility.PRIVATE)) {
            throw new ForbiddenException("User does not have permission to view this post..");
        }

        if (canViewAuthorPosts(viewer, post.getAuthor())) {
            return;
        }

        throw new ForbiddenException("User does not have permission to view this post..");
    }

    /**
     * Versão em lote de canViewAuthorPosts. Decide o mesmo para todos os autores de uma
     * vez, com um número fixo de consultas em vez de duas ou três por autor — o feed de
     * "Seguindo" chega aqui com a lista inteira de quem o usuário segue.
     */
    public List<User> filterViewableAuthors(User viewer, List<User> authors) {

        if (authors.isEmpty()) {
            return List.of();
        }

        Map<UUID, ProfileVisibility> visibilityByAuthor = userPrivacySettingsRepository.findAllByUserIn(authors)
                .stream()
                .collect(Collectors.toMap(settings -> settings.getUser().getId(), UserPrivacySettings::getPostsVisibility));

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

        // PRIVATE nunca entra, e um autor sem linha de privacidade também não: sem saber
        // a regra dele, o lado seguro é omitir em vez de expor.
        return authors.stream()
                .filter(author -> author.getId().equals(viewer.getId()) || viewable.contains(author.getId()))
                .toList();
    }

    public boolean canViewAuthorPosts(User viewer, User author) {

        if (viewer.getId().equals(author.getId())) {
            return true;
        }

        ProfileVisibility postsVisibility = userPrivacySettingsRepository.findByUser(author).getPostsVisibility();

        return switch (postsVisibility) {
            case PUBLIC -> true;
            case FOLLOWERS -> userFollowRepository.existsByFollowerAndFollowed(viewer, author);
            case FRIENDS -> userFriendshipRepository.existsMutualFriendship(viewer, author);
            case PRIVATE -> false;
        };
    }
}
