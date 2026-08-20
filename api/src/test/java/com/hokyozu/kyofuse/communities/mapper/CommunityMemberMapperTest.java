package com.hokyozu.kyofuse.communities.mapper;

import com.hokyozu.kyofuse.communities.dto.response.CommunityMemberResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommunityMemberMapperTest {

    @Test
    void canInstantiateMapper() {
        assertThat(new CommunityMemberMapper()).isNotNull();
    }

    @Test
    void toEntity_shouldBuildActiveMemberWithMemberRole() {
        Community community = community();
        User user = user("newmember");
        Instant before = Instant.now();

        CommunityMember member = CommunityMemberMapper.toEntity(user, community);

        assertThat(member.getCommunity()).isSameAs(community);
        assertThat(member.getUser()).isSameAs(user);
        assertThat(member.getRole()).isEqualTo(CommunityMemberRole.MEMBER);
        assertThat(member.getStatus()).isEqualTo(CommunityMemberStatus.ACTIVE);
        assertThat(member.getJoinedAt()).isBetween(before, Instant.now());
        assertThat(member.getCreatedAt()).isBetween(before, Instant.now());
        assertThat(member.getUpdatedAt()).isBetween(before, Instant.now());
        assertThat(member.getLeftAt()).isNull();
    }

    @Test
    void toResponse_shouldMapMemberFields() {
        Community community = community();
        User user = user("member1");
        Instant now = Instant.now();
        CommunityMember member = CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .role(CommunityMemberRole.ADMIN)
                .status(CommunityMemberStatus.ACTIVE)
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        GamerProfile profile = GamerProfile.builder()
                .nickname("Member One")
                .avatarUrl("https://example.com/avatar.png")
                .build();

        CommunityMemberResponse response = CommunityMemberMapper.toResponse(member, profile);

        assertThat(response.id()).isEqualTo(member.getId());
        assertThat(response.communityId()).isEqualTo(community.getId());
        assertThat(response.communityName()).isEqualTo(community.getName());
        assertThat(response.communitySlug()).isEqualTo(community.getSlug());
        assertThat(response.memberId()).isEqualTo(user.getId());
        assertThat(response.memberUsername()).isEqualTo(user.getUsername());
        assertThat(response.memberNickname()).isEqualTo("Member One");
        assertThat(response.memberAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(response.role()).isEqualTo(CommunityMemberRole.ADMIN);
        assertThat(response.status()).isEqualTo(CommunityMemberStatus.ACTIVE);
        assertThat(response.joinedAt()).isEqualTo(now);
        assertThat(response.leftAt()).isNull();
    }

    @Test
    void toResponse_shouldExposeLeftAtWhenMemberLeft() {
        Community community = community();
        User user = user("former-member");
        Instant now = Instant.now();
        CommunityMember member = CommunityMember.builder()
                .id(UUID.randomUUID())
                .community(community)
                .user(user)
                .role(CommunityMemberRole.MEMBER)
                .status(CommunityMemberStatus.LEFT)
                .joinedAt(now.minusSeconds(3600))
                .leftAt(now)
                .createdAt(now.minusSeconds(3600))
                .updatedAt(now)
                .build();

        CommunityMemberResponse response = CommunityMemberMapper.toResponse(member, null);

        assertThat(response.status()).isEqualTo(CommunityMemberStatus.LEFT);
        assertThat(response.leftAt()).isEqualTo(now);
        assertThat(response.memberNickname()).isNull();
        assertThat(response.memberAvatarUrl()).isNull();
    }

    private Community community() {
        return Community.builder()
                .id(UUID.randomUUID())
                .owner(user("owner"))
                .name("Kyofuse CS2")
                .slug("kyofuse-cs2")
                .visibility(CommunityVisibility.PUBLIC)
                .status(CommunityStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private User user(String username) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();
    }
}
