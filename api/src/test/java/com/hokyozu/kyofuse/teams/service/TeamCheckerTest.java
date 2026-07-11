package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamCheckerTest {

    private final TeamChecker teamChecker = new TeamChecker();

    @Test
    void checkUserIsOwner_shouldNotThrow_whenUserIsTeamOwner() {
        User owner = createUser();
        Team team = createTeam(owner);

        teamChecker.checkUserIsOwner(team, owner);
    }

    @Test
    void checkUserIsOwner_shouldThrowException_whenUserIsNotOwner() {
        User owner = createUser();
        User nonOwner = createUser();
        Team team = createTeam(owner);

        assertThatThrownBy(() -> teamChecker.checkUserIsOwner(team, nonOwner))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void checkUserIsOwner_shouldThrowException_whenTeamIsNull() {
        User user = createUser();

        assertThatThrownBy(() -> teamChecker.checkUserIsOwner(null, user))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void checkUserIsOwner_shouldThrowException_whenUserIsNull() {
        Team team = createTeam(createUser());

        assertThatThrownBy(() -> teamChecker.checkUserIsOwner(team, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void checkInactive_shouldNotThrow_whenStatusIsActive() {
        User owner = createUser();
        Team team = createTeam(owner);
        team.setStatus(TeamStatus.ACTIVE);

        teamChecker.checkInactive(team);
    }

    @Test
    void checkInactive_shouldThrowException_whenStatusIsInactive() {
        User owner = createUser();
        Team team = createTeam(owner);
        team.setStatus(TeamStatus.INACTIVE);

        assertThatThrownBy(() -> teamChecker.checkInactive(team))
                .isInstanceOf(BadRequestException.class);
    }

    private Team createTeam(User owner) {
        return Team.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .name("Test Team")
                .slug("test-team")
                .status(TeamStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private User createUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .username("teamuser" + System.nanoTime())
                .email("user" + System.nanoTime() + "@example.com")
                .firstName("Team")
                .lastName("User")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
