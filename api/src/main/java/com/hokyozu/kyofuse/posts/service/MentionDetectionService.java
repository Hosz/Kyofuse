package com.hokyozu.kyofuse.posts.service;

import com.hokyozu.kyofuse.comments.entity.Comment;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.notifications.dto.request.CreateNotificationRequest;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.notifications.service.NotificationService;
import com.hokyozu.kyofuse.posts.entity.Post;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MentionDetectionService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final CommunityRepository communityRepository;
    private final NotificationService notificationService;

    // Prefixes: @ for users, $ for teams, // for communities
    private static final Pattern USER_PATTERN = Pattern.compile("(?:^|\\s)@([A-Za-z0-9_.-]+)");
    private static final Pattern TEAM_PATTERN = Pattern.compile("(?:^|\\s)\\$([A-Za-z0-9_.-]+)");
    private static final Pattern COMMUNITY_PATTERN = Pattern.compile("(?:^|\\s)//([A-Za-z0-9_.-]+)");

    @Async
    @Transactional
    public void processMentions(Post post) {
        String content = post.getContent();
        if (content == null || content.isEmpty()) return;

        processUserMentions(content, post.getAuthor(), post, null);
        processTeamMentions(content, post.getAuthor(), post, null);
        processCommunityMentions(content, post.getAuthor(), post, null);
    }

    @Async
    @Transactional
    public void processCommentMentions(Comment comment) {
        String content = comment.getContent();
        if (content == null || content.isEmpty()) return;

        processUserMentions(content, comment.getAuthor(), comment.getPost(), comment);
        processTeamMentions(content, comment.getAuthor(), comment.getPost(), comment);
        processCommunityMentions(content, comment.getAuthor(), comment.getPost(), comment);
    }

    private void processUserMentions(String content, User author, Post post, Comment comment) {
        Matcher matcher = USER_PATTERN.matcher(content);
        Set<String> mentions = new HashSet<>();
        while (matcher.find()) mentions.add(matcher.group(1));

        for (String mention : mentions) {
            userRepository.findByUsername(mention).ifPresent(user -> {
                if (!user.getId().equals(author.getId())) {
                    sendMentionNotification(user, author, post, comment, NotificationType.POST_MENTION, NotificationType.COMMENT_MENTION);
                }
            });
        }
    }

    private void processTeamMentions(String content, User author, Post post, Comment comment) {
        Matcher matcher = TEAM_PATTERN.matcher(content);
        Set<String> mentions = new HashSet<>();
        while (matcher.find()) mentions.add(matcher.group(1));

        for (String mention : mentions) {
            teamRepository.findBySlug(mention).ifPresent(team -> {
                if (!team.getOwner().getId().equals(author.getId())) {
                    sendMentionNotification(team.getOwner(), author, post, comment, NotificationType.TEAM_MENTION, NotificationType.TEAM_MENTION);
                }
            });
        }
    }

    private void processCommunityMentions(String content, User author, Post post, Comment comment) {
        Matcher matcher = COMMUNITY_PATTERN.matcher(content);
        Set<String> mentions = new HashSet<>();
        while (matcher.find()) mentions.add(matcher.group(1));

        for (String mention : mentions) {
            communityRepository.findBySlug(mention).ifPresent(community -> {
                if (!community.getOwner().getId().equals(author.getId())) {
                    sendMentionNotification(community.getOwner(), author, post, comment, NotificationType.COMMUNITY_MENTION, NotificationType.COMMUNITY_MENTION);
                }
            });
        }
    }

    private void sendMentionNotification(User recipient, User actor, Post post, Comment comment, NotificationType postType, NotificationType commentType) {
        boolean isComment = comment != null;
        NotificationType type = isComment ? commentType : postType;
        NotificationTargetType targetType = isComment ? NotificationTargetType.COMMENT : NotificationTargetType.POST;
        String title = isComment ? "Você foi mencionado em um comentário!" : "Você foi mencionado em uma publicação!";
        String message = actor.getUsername() + " mencionou você.";
        
        notificationService.createNotification(
                CreateNotificationRequest.builder()
                        .recipient(recipient)
                        .actor(actor)
                        .type(type)
                        .title(title)
                        .message(message)
                        .targetType(targetType)
                        .targetId(isComment ? comment.getId() : post.getId())
                        .metadata(isComment ? Map.of("commentId", comment.getId().toString(), "postId", post.getId().toString()) : Map.of("postId", post.getId().toString()))
                        .build()
        );
    }
}
