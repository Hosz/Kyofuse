package com.hokyozu.kyofuse.relationships.permission.service.post;

import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostPermissionService {

    private final BlockValidator blockValidator;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;
    private final TeamMemberRepository teamMemberRepository;

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

    private void validatePrivateProfileAccess(User viewer, Post post) {

        blockValidator.validate(viewer, post.getAuthor());

        if (post.getVisibility().equals(PostVisibility.PUBLIC)) {
            return;
        }

        if (post.getVisibility().equals(PostVisibility.TEAM_ONLY) && areTeammates(viewer, post.getAuthor())) {
            return;
        }

        if (userPrivacySettingsRepository.findByUser(post.getAuthor()).getProfileVisibility().equals(ProfileVisibility.PUBLIC)) {
            return;
        }

        throw new ForbiddenException("User does not have permission to view this post..");
    }
}
