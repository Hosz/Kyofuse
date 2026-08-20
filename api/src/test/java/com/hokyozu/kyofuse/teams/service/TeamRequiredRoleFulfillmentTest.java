package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.repository.TeamRequiredRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * A vaga anunciada some quando alguém assume a função. Estes testes travam tanto o
 * caso em que ela some quanto os casos em que nada deve acontecer.
 */
@ExtendWith(MockitoExtension.class)
class TeamRequiredRoleFulfillmentTest {

    @Mock
    private TeamRequiredRoleRepository teamRequiredRoleRepository;

    @InjectMocks
    private TeamRequiredRoleFulfillment fulfillment;

    @Test
    void removesTheAnnouncedRoleWhenSomeoneTakesIt() {
        Team team = team();
        TeamRequiredRole required = TeamRequiredRole.builder()
                .id(UUID.randomUUID())
                .team(team)
                .roleName(PlayerRole.AWPER)
                .build();
        when(teamRequiredRoleRepository.findByTeamIdAndRoleName(team.getId(), PlayerRole.AWPER))
                .thenReturn(List.of(required));

        fulfillment.fulfill(team, PlayerRole.AWPER);

        verify(teamRequiredRoleRepository).deleteAll(List.of(required));
    }

    @Test
    void doesNothingWhenTheTeamWasNotLookingForThatRole() {
        Team team = team();
        when(teamRequiredRoleRepository.findByTeamIdAndRoleName(team.getId(), PlayerRole.IGL))
                .thenReturn(List.of());

        fulfillment.fulfill(team, PlayerRole.IGL);

        verify(teamRequiredRoleRepository, never()).deleteAll(anyList());
    }

    @Test
    void doesNothingWhenTheMemberWasEditedWithoutARole() {
        // editMember aceita mudar só o tipo de membro; nesse caso roleInTeam vem nulo e
        // nenhuma vaga deve ser fechada.
        fulfillment.fulfill(team(), null);

        verifyNoInteractions(teamRequiredRoleRepository);
    }

    @Test
    void doesNothingWithoutATeam() {
        fulfillment.fulfill(null, PlayerRole.SUPPORT);

        verifyNoInteractions(teamRequiredRoleRepository);
    }

    @Test
    void closesOnlyTheRoleThatWasFilled() {
        Team team = team();
        TeamRequiredRole awper = TeamRequiredRole.builder()
                .id(UUID.randomUUID())
                .team(team)
                .roleName(PlayerRole.AWPER)
                .build();
        when(teamRequiredRoleRepository.findByTeamIdAndRoleName(team.getId(), PlayerRole.AWPER))
                .thenReturn(List.of(awper));

        fulfillment.fulfill(team, PlayerRole.AWPER);

        // As outras vagas do time continuam abertas: só a consulta pela função assumida
        // chega ao banco.
        verify(teamRequiredRoleRepository).findByTeamIdAndRoleName(team.getId(), PlayerRole.AWPER);
        verify(teamRequiredRoleRepository, never()).findByTeamId(any());
    }

    private static Team team() {
        return Team.builder().id(UUID.randomUUID()).name("Kyofuse").build();
    }
}
