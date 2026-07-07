package com.hokyozu.kyofuse.teams.finder;

import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TeamFinder {

    private final TeamRepository teamRepository;

    public Team findTeamById(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado: " + teamId));
    }
}
