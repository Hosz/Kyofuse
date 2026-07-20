package com.hokyozu.kyofuse.relationships.permission.service.comment;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.posts.enums.PostVisibility;
import com.hokyozu.kyofuse.relationships.privacy.enums.ProfileVisibility;
import com.hokyozu.kyofuse.relationships.privacy.repository.UserPrivacySettingsRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentPermissionService {

    private final BlockValidator blockValidator;
    private final TeamMemberRepository teamMemberRepository;
    private final UserPrivacySettingsRepository userPrivacySettingsRepository;

    public void validateViewComment(User viewer, Comment comment) {

        if (comment.getPost().getAuthor().equals(viewer)) {
            return;
        }

        validatePrivateProfileAccess(viewer, comment.getPost());

        throw new ForbiddenException("User does not have permission to view this comment.");
    }

    public void validateReact(User user, Comment comment) {

        validatePrivateProfileAccess(user, comment.getPost());

        throw new ForbiddenException("User does not have permission to react to this comment.");
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
    }
}
