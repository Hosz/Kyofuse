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

    @Transactional
    public TeamMemberResponse addMember(UUID teamId, UUID userId, UUID userInvitedId) {
        User user = userFinder.findProfileByUserId(userId);
        User userInvited = userFinder.findProfileByUserId(userInvitedId);
        Team team = teamFinder.findTeamById(teamId);

        if (user.getId() != team.getOwner().getId()) {
            throw new BadRequestException("Only the team owner can add members.");
        }

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

        if (!user.getId().equals(team.getOwner().getId())) {
            throw new BadRequestException("Only the team owner can edit members.");
        }

        userChecker.checkActive(user);
        userChecker.checkActive(userEdited);

        if (!teamMemberRepository.existsByTeamAndUser(team, userEdited)) {
            throw new BadRequestException("User is not a member of the team.");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamAndUser(team, userEdited);

        TeamMemberMapper.toUpdate(teamMember, request);
        if (teamMember.getMemberType() != TeamMemberType.UNASSIGNED) {
            teamMember.setAssignmentDueAt(null);
        }

        teamMemberRepository.save(teamMember);
        return TeamMemberMapper.toResponse(teamMember);
    }
}
