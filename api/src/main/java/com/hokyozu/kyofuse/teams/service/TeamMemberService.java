package com.hokyozu.kyofuse.teams.service;

import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.teams.dto.request.TeamMemberEditRequest;
import com.hokyozu.kyofuse.teams.dto.response.TeamMemberResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.mapper.TeamMemberMapper;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final UserFinder userFinder;
    private final TeamFinder teamFinder;
    private final UserChecker userChecker;

    private final TeamMemberRepository teamMemberRepository;
    private final TeamChecker teamChecker;

    @Transactional
    public TeamMemberResponse addMember(UUID teamId, UUID userId, UUID userInvitedId) {
        User user = userFinder.findProfileByUserId(userId);
        User userInvited = userFinder.findProfileByUserId(userInvitedId);
        Team team = teamFinder.findTeamById(teamId);

        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        userChecker.checkActive(user);
        userChecker.checkActive(userInvited);

        if (teamMemberRepository.existsByTeamAndUser(team, userInvited)) {
            throw new BadRequestException("User is already a member of the team.");
        }

        TeamMember teamMember = TeamMemberMapper.toEntity(userInvited, team);
        TeamMember savedTeamMember = teamMemberRepository.save(teamMember);

        return TeamMemberMapper.toResponse(savedTeamMember);
    }

    @Transactional
    public TeamMemberResponse editMember(UUID teamId, UUID userEditedId, UUID userId, TeamMemberEditRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        User userEdited = userFinder.findProfileByUserId(userEditedId);
        Team team = teamFinder.findTeamById(teamId);

        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        userChecker.checkActive(user);
        userChecker.checkActive(userEdited);

        if (!teamMemberRepository.existsByTeamAndUser(team, userEdited)) {
            throw new BadRequestException("User is not a member of the team.");
        }

        if (request != null && request.roleInTeam() == null && request.memberType() == null) {
            throw new BadRequestException("At least one field must be provided for update.");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, userEdited);

        TeamMemberMapper.toUpdate(teamMember, request);
        if (teamMember.getMemberType() != TeamMemberType.UNASSIGNED) {
            teamMember.setAssignmentDueAt(null);
        }

        teamMemberRepository.save(teamMember);
        return TeamMemberMapper.toResponse(teamMember);
    }

    @Transactional(readOnly = true)
    public Page<TeamMemberResponse> listMembers(UUID teamId, UUID userId, Pageable pageable) {
        Team team = teamFinder.findTeamById(teamId);
        User user = userFinder.findProfileByUserId(userId);

        userChecker.checkActive(user);
        teamChecker.checkInactive(team);

        Page<TeamMember> teamMembers = teamMemberRepository.findByTeam(team, pageable);
        return teamMembers.map(TeamMemberMapper::toResponse);
    }

    @Transactional
    public void removeMember(UUID teamId, UUID userId, UUID userRemovedId) {
        User user = userFinder.findProfileByUserId(userId);
        User userRemoved = userFinder.findProfileByUserId(userRemovedId);
        Team team = teamFinder.findTeamById(teamId);

        teamChecker.checkInactive(team);
        teamChecker.checkUserIsOwner(team, user);

        userChecker.checkActive(user);
        userChecker.checkActive(userRemoved);

        teamChecker.checkUserIsOwner(team, user);

        if (team.getOwner().getId().equals(userRemoved.getId())) {
            throw new BadRequestException("Team owner cannot be removed from the team.");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, userRemoved);
        if (teamMember == null) {
            throw new BadRequestException("Member does not exist in this team.");
        }

        teamMemberRepository.delete(teamMember);
    }

    @Transactional
    public void leaveTeam(UUID teamId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        Team team = teamFinder.findTeamById(teamId);

        userChecker.checkActive(user);

        if (team.getOwner().getId().equals(user.getId())) {
            throw new BadRequestException("Team owner cannot leave the team.");
        }

        if (!teamMemberRepository.existsByTeamAndUser(team, user)) {
            throw new BadRequestException("User is not a member of the team.");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, user);
        if (teamMember == null) {
            throw new BadRequestException("Member does not exist in this team.");
        }

        teamMemberRepository.delete(teamMember);
    }

    @Transactional(readOnly = true)
    public TeamMemberResponse detailMember(UUID teamId, UUID userId, UUID teamMemberId) {
        Team team = teamFinder.findTeamById(teamId);
        User user = userFinder.findProfileByUserId(userId);
        User teamMemberUser = userFinder.findProfileByUserId(teamMemberId);

        userChecker.checkActive(user);
        teamChecker.checkInactive(team);

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, teamMemberUser);

        if (teamMember == null) {
            throw new BadRequestException("Team member not found.");
        }

        if (!teamMember.getTeam().getId().equals(team.getId())) {
            throw new BadRequestException("Team member does not belong to the specified team.");
        }

        return TeamMemberMapper.toResponse(teamMember);
    }
}
