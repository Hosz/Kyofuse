package com.hokyozu.kyofuse.shared;

import com.hokyozu.kyofuse.chat.enums.ConversationMemberRole;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.comments.enums.CommentStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.invites.enums.TeamInviteStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationStatus;
import com.hokyozu.kyofuse.notifications.enums.NotificationTargetType;
import com.hokyozu.kyofuse.notifications.enums.NotificationType;
import com.hokyozu.kyofuse.reactions.enums.ReactionType;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEnumTest {

    @Test
    void commentStatusValuesAreStable() {
        assertThat(CommentStatus.valueOf("ACTIVE")).isEqualTo(CommentStatus.ACTIVE);
        assertThat(CommentStatus.values()).containsExactly(
                CommentStatus.ACTIVE,
                CommentStatus.DELETED,
                CommentStatus.HIDDEN,
                CommentStatus.REPORTED
        );
    }

    @Test
    void inviteAndNotificationValuesAreStable() {
        assertThat(TeamInviteStatus.values()).containsExactly(
                TeamInviteStatus.PENDING,
                TeamInviteStatus.ACCEPTED,
                TeamInviteStatus.DECLINED,
                TeamInviteStatus.CANCELED
        );
        assertThat(NotificationStatus.values()).containsExactly(
                NotificationStatus.UNREAD,
                NotificationStatus.READ,
                NotificationStatus.ARCHIVED
        );
        assertThat(NotificationType.values()).containsExactly(
                NotificationType.FOLLOW_REQUEST_RECEIVED,
                NotificationType.FOLLOW_REQUEST_ACCEPTED,
                NotificationType.FOLLOW_REQUEST_DECLINED,
                NotificationType.FOLLOW_STARTED,
                NotificationType.TEAM_INVITE_RECEIVED,
                NotificationType.TEAM_INVITE_ACCEPTED,
                NotificationType.TEAM_INVITE_DECLINED,
                NotificationType.TEAM_INVITE_CANCELED,
                NotificationType.TEAM_MEMBER_ADDED,
                NotificationType.TEAM_MEMBER_REMOVED,
                NotificationType.TEAM_MEMBER_LEFT,
                NotificationType.TEAM_MEMBER_EDITED,
                NotificationType.NEW_POST,
                NotificationType.POST_COMMENT,
                NotificationType.POST_REACTION,
                NotificationType.COMMENT_REACTION,
                NotificationType.NEW_MESSAGE,
                NotificationType.MESSAGE_REQUEST,
                NotificationType.POST_MENTION,
                NotificationType.COMMENT_MENTION,
                NotificationType.TEAM_MENTION,
                NotificationType.COMMUNITY_MENTION,
                NotificationType.SYSTEM
        );
        assertThat(NotificationTargetType.values()).containsExactly(
                NotificationTargetType.POST,
                NotificationTargetType.COMMENT,
                NotificationTargetType.TEAM,
                NotificationTargetType.TEAM_INVITE,
                NotificationTargetType.FOLLOW,
                NotificationTargetType.CONVERSATION,
                NotificationTargetType.SYSTEM
        );
    }

    @Test
    void reactionAndTeamValuesAreStable() {
        assertThat(ReactionType.values()).containsExactly(
                ReactionType.LIKE,
                ReactionType.FIRE,
                ReactionType.CLUTCH,
                ReactionType.NICE_SHOT,
                ReactionType.LOL
        );
        assertThat(TeamStatus.values()).containsExactly(
                TeamStatus.ACTIVE,
                TeamStatus.RECRUITING,
                TeamStatus.CLOSED,
                TeamStatus.INACTIVE
        );
        assertThat(TeamMemberStatus.values()).containsExactly(
                TeamMemberStatus.ACTIVE,
                TeamMemberStatus.LEFT,
                TeamMemberStatus.REMOVED,
                TeamMemberStatus.KICKED
        );
        assertThat(TeamMemberType.values()).containsExactly(
                TeamMemberType.UNASSIGNED,
                TeamMemberType.PLAYER,
                TeamMemberType.SUBSTITUTE,
                TeamMemberType.COACH,
                TeamMemberType.MANAGER,
                TeamMemberType.ANALYST
        );
    }

    @Test
    void communityValuesAreStable() {
        assertThat(CommunityStatus.values()).containsExactly(
                CommunityStatus.ACTIVE,
                CommunityStatus.ARCHIVED
        );
        assertThat(CommunityVisibility.values()).containsExactly(
                CommunityVisibility.PUBLIC,
                CommunityVisibility.PRIVATE
        );
        assertThat(CommunityMemberRole.values()).containsExactly(
                CommunityMemberRole.ADMIN,
                CommunityMemberRole.MODERATOR,
                CommunityMemberRole.MEMBER
        );
        assertThat(CommunityMemberStatus.values()).containsExactly(
                CommunityMemberStatus.ACTIVE,
                CommunityMemberStatus.LEFT,
                CommunityMemberStatus.REMOVED,
                CommunityMemberStatus.KICKED,
                CommunityMemberStatus.BANNED
        );
    }

    @Test
    void chatValuesAreStable() {
        assertThat(ConversationType.values()).containsExactly(
                ConversationType.DIRECT,
                ConversationType.GROUP,
                ConversationType.COMMUNITY
        );
        assertThat(DirectConversationStatus.values()).containsExactly(
                DirectConversationStatus.PENDING,
                DirectConversationStatus.ACCEPTED,
                DirectConversationStatus.DECLINED
        );
        assertThat(ConversationMemberRole.values()).containsExactly(
                ConversationMemberRole.ADMIN,
                ConversationMemberRole.MEMBER
        );
        assertThat(ConversationMemberStatus.values()).containsExactly(
                ConversationMemberStatus.ACTIVE,
                ConversationMemberStatus.LEFT,
                ConversationMemberStatus.REMOVED,
                ConversationMemberStatus.KICKED
        );
    }
}
