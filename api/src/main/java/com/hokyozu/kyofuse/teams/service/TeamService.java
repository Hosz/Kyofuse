package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.profiles.enums.PlayerRole;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.teams.dto.request.TeamFilter;
import com.hokyozu.kyofuse.teams.dto.request.TeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequest;
import com.hokyozu.kyofuse.teams.dto.request.UpdateTeamRequiredRolesRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.teams.enums.TeamStatus;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.mapper.TeamMapper;
import com.hokyozu.kyofuse.teams.mapper.TeamRequiredRoleMapper;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRequiredRoleRepository;
import com.hokyozu.kyofuse.teams.specification.TeamSpecification;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final UserChecker userChecker;
    private final TeamChecker teamChecker;

    private final UserFinder userFinder;
    private final TeamFinder teamFinder;

    private final TeamRepository teamRepository;
    private final TeamRequiredRoleRepository teamRequiredRoleRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public TeamResponse createTeams(@Valid TeamRequest request, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        validateCreateTeamRequest(request, user);

        Team team = TeamMapper.toEntity(request, user);
        Team teamSaved = teamRepository.save(team);

        List<TeamRequiredRole> requiredRoles = requiredRolesOrEmpty(request).stream()
                        .distinct()
                        .map(role -> TeamRequiredRoleMapper.toEntity(team, role))
                        .toList();
        List<TeamRequiredRole> requiredRolesSaved = teamRequiredRoleRepository.saveAll(requiredRoles);

        return TeamMapper.toResponse(teamSaved, requiredRolesSaved);
    }

    @Transactional(readOnly = true)
    public TeamResponse detailTeam(UUID teamId) {

        Team team = teamFinder.findTeamById(teamId);
        teamChecker.checkInactive(team);

        List<TeamRequiredRole> requiredRoles = teamRequiredRoleRepository.findByTeamId(teamId);

        return TeamMapper.toResponse(team, requiredRoles);
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> listingTeams(TeamFilter filter, Pageable pageable) {
        Specification<Team> spec = Specification
                .where(TeamSpecification.nameContains(filter.name()))
                .and(TeamSpecification.hasSlug(filter.slug()))
                .and(TeamSpecification.hasStatus(filter.statusOrActive()))
                .and(TeamSpecification.hasRegion(filter.region()))
                .and(TeamSpecification.hasAnyRequiredRole(filter.requiredRoles()))
                .and(TeamSpecification.minPremierRatingAtLeast(filter.minPremierRating()))
                .and(TeamSpecification.maxPremierRatingAtMost(filter.maxPremierRating()))
                .and(TeamSpecification.minFaceitLevelAtLeast(filter.minFaceitLevel()))
                .and(TeamSpecification.maxFaceitLevelAtMost(filter.maxFaceitLevel()))
                .and(TeamSpecification.minGcRankAtLeast(filter.minGcRank()))
                .and(TeamSpecification.maxGcRankAtMost(filter.maxGcRank()));

        if (filter.status() == TeamStatus.INACTIVE) {
            throw new BadRequestException("Filtro de status inválido.");
        }

        Page<Team> teams = teamRepository.findAll(spec, pageable);
        List<UUID> teamIds = teams.stream()
                .map(Team::getId)
                .toList();

        Map<UUID, List<TeamRequiredRole>> rolesByTeamId = teamIds.isEmpty()
                ? Map.of()
                : teamRequiredRoleRepository.findByTeamIdIn(teamIds).stream()
                        .collect(Collectors.groupingBy(role -> role.getTeam().getId()));

        return teams.map(team -> TeamMapper.toResponse(
                team,
                rolesByTeamId.getOrDefault(team.getId(), List.of())
        ));
    }

    @Transactional
    public TeamResponse editTeam(UUID userId, UUID teamId, UpdateTeamRequest updateTeamRequest) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Team team = teamFinder.findTeamById(teamId);
        teamChecker.checkInactive(team);

        List<TeamRequiredRole> requiredRoles = teamRequiredRoleRepository.findByTeamId(teamId);

        teamChecker.checkUserIsOwner(team, user);

        if (updateTeamRequest.status() == TeamStatus.INACTIVE) {
            throw new BadRequestException("Use o endpoint de inativação para inativar o time.");
        }

        if (updateTeamRequest.name() != null && updateTeamRequest.name().strip().isBlank()) {
            throw new BadRequestException("O nome do time não pode ser vazio.");
        } else if (Objects.equals(team.getName(), updateTeamRequest.name())) {
            throw new BadRequestException("O nome do time não foi alterado.");
        }

        if (updateTeamRequest.description() != null && updateTeamRequest.description().strip().isBlank()) {
            throw new BadRequestException("A descrição do time não pode ser vazio.");
        } else if (Objects.equals(team.getDescription(), updateTeamRequest.description())) {
            throw new BadRequestException("A descrição do time não foi alterada.");
        }

        if (updateTeamRequest.region() != null && updateTeamRequest.region().strip().isBlank()) {
            throw new BadRequestException("A região do time não pode ser vazio.");
        } else if (Objects.equals(team.getRegion(), updateTeamRequest.region()) && updateTeamRequest.region() != null ) {
            throw new BadRequestException("A região do time não foi alterada.");
        }

        if (updateTeamRequest.status() != null && updateTeamRequest.status().equals(team.getStatus())) {
            throw new BadRequestException("O status do time não foi alterado.");
        }
        if (updateTeamRequest.minGcRank() != null && updateTeamRequest.minGcRank().equals(team.getMinGcRank())) {
            throw new BadRequestException("O minGcRank do time não foi alterado.");
        }
        if (updateTeamRequest.maxGcRank() != null && updateTeamRequest.maxGcRank().equals(team.getMaxGcRank())) {
            throw new BadRequestException("O maxGcRank do time não foi alterado.");
        }
        if (updateTeamRequest.minFaceitLevel() != null && updateTeamRequest.minFaceitLevel().equals(team.getMinFaceitLevel())) {
            throw new BadRequestException("O minFaceitLevel do time não foi alterado.");
        }
        if (updateTeamRequest.maxFaceitLevel() != null && updateTeamRequest.maxFaceitLevel().equals(team.getMaxFaceitLevel())) {
            throw new BadRequestException("O maxFaceitLevel do time não foi alterado.");
        }

        TeamMapper.toUpdate(team, updateTeamRequest);
        validateTeamRanges(team);
        Team updatedTeam = teamRepository.save(team);

        return TeamMapper.toResponse(updatedTeam, requiredRoles);
    }

    @Transactional
    public TeamResponse inactiveTeam(UUID userId, UUID teamId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Team team = teamFinder.findTeamById(teamId);
        teamChecker.checkInactive(team);

        teamChecker.checkUserIsOwner(team, user);

        List<TeamRequiredRole> requiredRoles = teamRequiredRoleRepository.findByTeamId(teamId);

        team.setStatus(TeamStatus.INACTIVE);
        team.setUpdatedAt(Instant.now());
        Team teamSaved = teamRepository.save(team);

        return TeamMapper.toResponse(teamSaved, requiredRoles);
    }

    @Transactional
    public TeamResponse manageRequiredRoles(UUID userId, UUID teamId, UpdateTeamRequiredRolesRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NotFoundException("Time não encontrado: " + teamId));
        teamChecker.checkInactive(team);

        teamChecker.checkUserIsOwner(team, user);

        validateRequiredRoles(request.requiredRoles());

        List<TeamRequiredRole> currentRoles = teamRequiredRoleRepository.findByTeamId(teamId);

        teamRequiredRoleRepository.deleteAll(currentRoles);
        teamRequiredRoleRepository.flush();

        List<TeamRequiredRole> updatedRoles = request.requiredRoles().stream()
                .distinct()
                .map(role -> TeamRequiredRoleMapper.toEntity(team, role))
                .toList();

        List<TeamRequiredRole> savedRoles = teamRequiredRoleRepository.saveAll(updatedRoles);

        team.setUpdatedAt(Instant.now());
        Team teamSaved = teamRepository.save(team);

        return TeamMapper.toResponse(teamSaved, savedRoles);
    }

    private void validateCreateTeamRequest(TeamRequest request, User user) {
        userChecker.checkActive(user);

        if (teamRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Slug já está em uso.");
        }
    }

    private List<PlayerRole> requiredRolesOrEmpty(TeamRequest request) {
        return request.requiredRoles() == null ? List.of() : request.requiredRoles();
    }

    private void validateTeamRanges(Team team) {
        if (team.getMinPremierRating() != null
                && team.getMaxPremierRating() != null
                && team.getMinPremierRating() > team.getMaxPremierRating()) {
            throw new BadRequestException("minPremierRating must be less than or equal to maxPremierRating");
        }

        if (team.getMinFaceitLevel() != null
                && team.getMaxFaceitLevel() != null
                && team.getMinFaceitLevel() > team.getMaxFaceitLevel()) {
            throw new BadRequestException("minFaceitLevel must be less than or equal to maxFaceitLevel");
        }

        if (team.getMinGcRank() != null
                && team.getMaxGcRank() != null
                && team.getMinGcRank() > team.getMaxGcRank()) {
            throw new BadRequestException("minGcRank must be less than or equal to maxGcRank");
        }
    }

    private void validateRequiredRoles(List<PlayerRole> requiredRoles) {
        if (requiredRoles == null) {
            throw new BadRequestException("requiredRoles não pode ser nulo.");
        }

        if (requiredRoles.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("requiredRoles não pode conter valores nulos.");
        }

        if (requiredRoles.stream().distinct().count() != requiredRoles.size()) {
            throw new BadRequestException("requiredRoles não pode conter roles duplicadas.");
        }
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> listingMyTeams(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        List<TeamMember> membro = teamMemberRepository.findByUser(user);

        List<UUID> teamIds = membro.stream()
                .map(tm -> tm.getTeam().getId())
                .toList();

        Page<Team> teams = teamRepository.findByIdIn(teamIds, pageable);
        return teams.map(team -> {
            List<TeamRequiredRole> requiredRoles = teamRequiredRoleRepository.findByTeamId(team.getId());
            return TeamMapper.toResponse(team, requiredRoles);
        });
    }
}
