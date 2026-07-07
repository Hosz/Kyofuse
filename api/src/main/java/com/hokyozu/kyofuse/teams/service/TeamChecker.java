package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TeamChecker {
    public void checkInactive(Team team) {
        if (team.getStatus() == TeamStatus.INACTIVE) {
            throw new BadRequestException("Time inativo.");
        }
    }

    public void checkUserIsOwner(Team team, User user) {
        if (!user.getId().equals(team.getOwner().getId())) {
            throw new BadRequestException("Usuário não é o dono do time.");
        }
    }
}
