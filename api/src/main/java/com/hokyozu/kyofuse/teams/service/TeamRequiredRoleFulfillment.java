package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.repository.TeamRequiredRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Uma vaga anunciada deixa de ser vaga assim que alguém assume a função — seja um
 * membro novo aceitando o convite, seja um que já estava e mudou de função.
 *
 * A remoção é definitiva: se o dono voltar a precisar da função, ele a anuncia de novo
 * em "Papéis Necessários". Sem isso, a vaga continuaria aberta depois de preenchida e
 * o time apareceria procurando alguém que já tem.
 */
@Component
@RequiredArgsConstructor
public class TeamRequiredRoleFulfillment {

    private final TeamRequiredRoleRepository teamRequiredRoleRepository;

    public void fulfill(Team team, PlayerRole role) {
        if (team == null || role == null) {
            return;
        }

        List<TeamRequiredRole> matching = teamRequiredRoleRepository.findByTeamIdAndRoleName(team.getId(), role);
        if (matching.isEmpty()) {
            return;
        }

        teamRequiredRoleRepository.deleteAll(matching);
    }
}
