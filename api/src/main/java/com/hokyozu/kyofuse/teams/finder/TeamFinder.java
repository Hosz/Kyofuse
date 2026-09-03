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

    public Team findTeamBySlug(String slug) {
        return teamRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Time não encontrado: " + slug));
    }

    public Team findTeamById(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado: " + teamId));
    }

    public Team findTeamByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new NotFoundException("Time não encontrado.");
        }
        try {
            return findTeamById(UUID.fromString(identifier.trim()));
        } catch (IllegalArgumentException e) {
            return findTeamBySlug(identifier.trim());
        }
    }
}
