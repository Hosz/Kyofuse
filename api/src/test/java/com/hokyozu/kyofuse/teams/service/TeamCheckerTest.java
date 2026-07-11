package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamCheckerTest {

    private TeamChecker teamChecker;

    @BeforeEach
    void setUp() {
        teamChecker = new TeamChecker();
    }

    @Test
    void checkInactive_shouldThrowException_whenTeamIsInactive() {
        Team inactiveTeam = new Team();
        inactiveTeam.setStatus(TeamStatus.INACTIVE);

        assertThatThrownBy(() -> teamChecker.checkInactive(inactiveTeam))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Time inativo.");
    }

    @Test
    void checkInactive_shouldNotThrowException_whenTeamIsActive() {
        Team activeTeam = new Team();
        activeTeam.setStatus(TeamStatus.ACTIVE);

        assertThatNoException()
                .isThrownBy(() -> teamChecker.checkInactive(activeTeam));
    }

    @Test
    void checkInactive_shouldNotThrowException_whenTeamIsPending() {
        Team pendingTeam = new Team();
        pendingTeam.setStatus(TeamStatus.PENDING);

        assertThatNoException()
                .isThrownBy(() -> teamChecker.checkInactive(pendingTeam));
    }

    @Test
    void checkUserIsOwner_shouldNotThrowException_whenUserIsOwner() {
        UUID userId = UUID.randomUUID();
        User owner = new User();
        owner.setId(userId);
        owner.setUsername("owner");
        owner.setEmail("owner@test.com");
        owner.setRole(UserRole.USER);
        owner.setStatus(UserStatus.ACTIVE);

        Team team = new Team();
        team.setOwner(owner);

        assertThatNoException()
                .isThrownBy(() -> teamChecker.checkUserIsOwner(team, owner));
    }

    @Test
    void checkUserIsOwner_shouldThrowException_whenUserIsNotOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);
        owner.setUsername("owner");
        owner.setEmail("owner@test.com");
        owner.setRole(UserRole.USER);
        owner.setStatus(UserStatus.ACTIVE);

        User notOwner = new User();
        notOwner.setId(userId);
        notOwner.setUsername("not_owner");
        notOwner.setEmail("not_owner@test.com");
        notOwner.setRole(UserRole.USER);
        notOwner.setStatus(UserStatus.ACTIVE);

        Team team = new Team();
        team.setOwner(owner);

        assertThatThrownBy(() -> teamChecker.checkUserIsOwner(team, notOwner))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Usuário não é o dono do time.");
    }

    @Test
    void checkUserIsOwner_shouldThrowException_whenOwnerIsNull() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("user");
        user.setEmail("user@test.com");

        Team team = new Team();
        team.setOwner(null);

        assertThatThrownBy(() -> teamChecker.checkUserIsOwner(team, user))
                .isInstanceOf(NullPointerException.class);
    }
}
