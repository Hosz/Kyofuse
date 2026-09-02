package com.hokyozu.kyofuse.relationships.permission.service.comment;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.relationships.permission.service.post.PostPermissionService;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentPermissionService {

    private final PostPermissionService postPermissionService;

    public void validateViewComment(User viewer, Comment comment) {
        try {
            postPermissionService.validateViewPost(viewer, comment.getPost());
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to view this comment.");
        }
    }

    public void validateReact(User user, Comment comment) {
        try {
            postPermissionService.validateViewPost(user, comment.getPost());
        } catch (ForbiddenException e) {
            throw new ForbiddenException("User does not have permission to react to this comment.");
        }
    }
}
