package com.hokyozu.kyofuse.teams.finder;

import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamFinderTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamFinder teamFinder;

    @Test
    void findTeamById_shouldReturnTeam_whenTeamExists() {
        UUID teamId = UUID.randomUUID();
        Team expectedTeam = createTeam(teamId, "Test Team", TeamStatus.ACTIVE);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(expectedTeam));

        Team foundTeam = teamFinder.findTeamById(teamId);

        assertThat(foundTeam).isNotNull();
        assertThat(foundTeam.getId()).isEqualTo(teamId);
        assertThat(foundTeam.getName()).isEqualTo("Test Team");
        assertThat(foundTeam.getStatus()).isEqualTo(TeamStatus.ACTIVE);
    }

    @Test
    void findTeamById_shouldThrowNotFoundException_whenTeamNotFound() {
        UUID teamId = UUID.randomUUID();

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamFinder.findTeamById(teamId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Time não encontrado")
                .hasMessageContaining(teamId.toString());
    }

    @Test
    void findTeamById_shouldThrowNotFoundException_withCorrectMessage() {
        UUID teamId = UUID.randomUUID();

        when(teamRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamFinder.findTeamById(teamId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Time não encontrado: " + teamId);
    }

    @Test
    void findTeamById_shouldReturnInactiveTeam_whenTeamIsInactive() {
        UUID teamId = UUID.randomUUID();
        Team inactiveTeam = createTeam(teamId, "Inactive Team", TeamStatus.INACTIVE);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(inactiveTeam));

        Team foundTeam = teamFinder.findTeamById(teamId);

        assertThat(foundTeam).isNotNull();
        assertThat(foundTeam.getStatus()).isEqualTo(TeamStatus.INACTIVE);
    }

    @Test
    void findTeamById_shouldReturnRecruitingTeam_whenTeamIsRecruiting() {
        UUID teamId = UUID.randomUUID();
        Team recruitingTeam = createTeam(teamId, "Recruiting Team", TeamStatus.RECRUITING);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(recruitingTeam));

        Team foundTeam = teamFinder.findTeamById(teamId);

        assertThat(foundTeam).isNotNull();
        assertThat(foundTeam.getStatus()).isEqualTo(TeamStatus.RECRUITING);
    }

    private Team createTeam(UUID teamId, String name, TeamStatus status) {
        Team team = new Team();
        team.setId(teamId);
        team.setName(name);
        team.setStatus(status);
        team.setOwner(createUser());
        return team;
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
