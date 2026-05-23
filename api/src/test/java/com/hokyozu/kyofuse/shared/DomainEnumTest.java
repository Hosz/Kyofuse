package com.hokyozu.kyofuse.shared;

import com.hokyozu.kyofuse.comments.enums.CommentStatus;
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
                NotificationType.PENDING,
                NotificationType.ACCEPTED,
                NotificationType.DECLINED,
                NotificationType.CANCELED
        );
        assertThat(NotificationTargetType.values()).containsExactly(
                NotificationTargetType.POST,
                NotificationTargetType.COMMENT,
                NotificationTargetType.TEAM,
                NotificationTargetType.TEAM_INVITE,
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
}
