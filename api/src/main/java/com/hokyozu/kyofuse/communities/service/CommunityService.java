package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.chat.service.ConversationService;
import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.mapper.CommunityMapper;
import com.hokyozu.kyofuse.communities.mapper.CommunityMemberMapper;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.validator.CommunityCreationValidator;
import com.hokyozu.kyofuse.communities.validator.CommunityEditValidator;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberRole;
import com.hokyozu.kyofuse.communities.repository.CommunityJoinRequestRepository;
import com.hokyozu.kyofuse.communities.repository.UserPinnedCommunityRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.storage.service.ImageProcessingService;
import com.hokyozu.kyofuse.teams.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.Instant;
import com.hokyozu.kyofuse.teams.dto.response.TeamResponse;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.entity.TeamMember;
import com.hokyozu.kyofuse.teams.enums.TeamMemberStatus;
import com.hokyozu.kyofuse.teams.enums.TeamMemberType;
import com.hokyozu.kyofuse.teams.finder.TeamFinder;
import com.hokyozu.kyofuse.teams.mapper.TeamMapper;
import com.hokyozu.kyofuse.teams.mapper.TeamMemberMapper;
import com.hokyozu.kyofuse.teams.repository.TeamMemberRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.teams.repository.TeamRequiredRoleRepository;
import com.hokyozu.kyofuse.teams.service.TeamChecker;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;

    private final CommunityCreationValidator communityCreationValidator;
    private final CommunityEditValidator communityEditValidator;

    private final ConversationService conversationService;
    private final ImageProcessingService imageProcessingService;

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityJoinRequestRepository communityJoinRequestRepository;
    private final UserPinnedCommunityRepository userPinnedCommunityRepository;
    private final TeamRepository teamRepository;
    private final TeamChecker teamChecker;
    private final TeamFinder teamFinder;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRequiredRoleRepository teamRequiredRoleRepository;

    @Autowired
    @Lazy
    private TeamService teamService;

    @CacheEvict(value = "communities_public", allEntries = true)
    @Transactional
    public CommunityResponse uploadAvatar(UUID userId, UUID communityId, MultipartFile file) throws IOException {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Community community = findCommunityById(communityId);
        if (!community.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the owner can update the community avatar");
        }
        String avatarUrl = imageProcessingService.processAndUploadAvatar(communityId, "communities", file);
        community.setAvatarUrl(avatarUrl);
        communityRepository.save(community);
        return CommunityMapper.toResponse(community);
    }

    @CacheEvict(value = "communities_public", allEntries = true)
    @Transactional
    public CommunityResponse uploadBanner(UUID userId, UUID communityId, MultipartFile file) throws IOException {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);
        Community community = findCommunityById(communityId);
        if (!community.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the owner can update the community banner");
        }
        String bannerUrl = imageProcessingService.processAndUploadBanner(communityId, "communities", file);
        community.setBannerUrl(bannerUrl);
        communityRepository.save(community);
        return CommunityMapper.toResponse(community);
    }

    private Community findCommunityById(UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));
        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new NotFoundException("Community not found");
        }
        return community;
    }

    public Community findCommunityByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new NotFoundException("Community not found");
        }
        Community community;
        try {
            community = communityRepository.findById(UUID.fromString(identifier.trim()))
                    .orElseThrow(() -> new NotFoundException("Community not found"));
        } catch (IllegalArgumentException e) {
            community = communityRepository.findBySlug(identifier.trim())
                    .orElseThrow(() -> new NotFoundException("Community not found"));
        }
        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new NotFoundException("Community not found");
        }
        return community;
    }

    @CacheEvict(value = "communities_public", allEntries = true)
    @Transactional
    public CommunityResponse createCommunity(@Valid CommunityRequest request, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        communityCreationValidator.validate(request);

        Community community = CommunityMapper.toEntity(request, user);
        communityRepository.save(community);
        communityMemberRepository.save(CommunityMemberMapper.toOwnerEntity(user, community));
        // Toda Community tem exatamente uma conversa COMMUNITY (doc.md 10.6) — sem ela a
        // comunidade aparece na listagem de chats do membro sem conversa correspondente.
        conversationService.createCommunityConversation(community, user);

        return CommunityMapper.toResponse(community);
    }

    /**
     * O slug nasce igual ao do Team, mas o slug de communities é único globalmente
     * (avulsas e de Team competem no mesmo namespace — ver doc.md 10.5). Como
     * teams.slug não impede colisão com uma comunidade avulsa que já exista com
     * aquele texto, resolve um slug disponível antes de criar em vez de deixar
     * estourar a constraint do banco e derrubar a criação do Team inteira.
     */
    @CacheEvict(value = "communities_public", allEntries = true)
    @Transactional
    public Community autoCreateTeamCommunity(User user, Team team) {
        String slug = resolveAvailableSlug(team.getSlug());
        Community community = CommunityMapper.toEntityTeamCommunity(user, team, slug);
        communityRepository.save(community);
        communityMemberRepository.save(CommunityMemberMapper.toOwnerEntity(user, community));
        return community;
    }

    private static final int SLUG_MAX_LENGTH = 100;

    private String resolveAvailableSlug(String baseSlug) {
        if (!communityRepository.existsBySlug(baseSlug)) {
            return baseSlug;
        }

        int suffix = 2;
        String candidate;
        do {
            String suffixText = "-" + suffix;
            // communities.slug é varchar(100) — trunca a base se o sufixo não couber.
            String base = baseSlug.length() + suffixText.length() > SLUG_MAX_LENGTH
                    ? baseSlug.substring(0, SLUG_MAX_LENGTH - suffixText.length())
                    : baseSlug;
            candidate = base + suffixText;
            suffix++;
        } while (communityRepository.existsBySlug(candidate));

        return candidate;
    }

    @CacheEvict(value = "communities_public", allEntries = true)
    @Transactional
    public CommunityResponse editCommunity(UUID userId, UUID communityId, @Valid UpdateCommunityRequest request) {
        User user = userFinder.findProfileByUserId(userId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));
        userChecker.checkActive(user);

        if (!community.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Community not found");
        }

        communityEditValidator.validate(request, community);

        CommunityMapper.toEdit(community, request);
        communityRepository.save(community);

        return CommunityMapper.toResponse(community);
    }

    @Cacheable(value = "communities_public", key = "#identifier")
    @Transactional(readOnly = true)
    public CommunityResponse detailCommunity(String identifier) {
        Community community = findCommunityByIdentifier(identifier);
        return CommunityMapper.toResponse(community);
    }

    @CacheEvict(value = {"communities_public", "teams_public"}, allEntries = true)
    @Transactional
    public void deleteCommunity(UUID userId, UUID communityId) {
        deleteCommunity(userId, communityId, false);
    }

    @CacheEvict(value = {"communities_public", "teams_public"}, allEntries = true)
    @Transactional
    public void deleteCommunity(UUID userId, UUID communityId, boolean deleteTeam) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (!community.getOwner().getId().equals(user.getId())) {
            throw new NotFoundException("Community not found");
        }

        Team team = community.getTeam();
        if (team != null) {
            if (deleteTeam) {
                if (!team.getOwner().getId().equals(user.getId())) {
                    throw new ForbiddenException("Apenas o dono do time pode solicitar a exclusão do mesmo.");
                }
                community.setTeam(null);
                communityRepository.save(community);
                if (teamService != null) {
                    teamService.deleteTeam(userId, team.getId(), false);
                }
            } else {
                community.setTeam(null);
                communityRepository.save(community);
            }
        }

        userPinnedCommunityRepository.deleteByCommunityId(community.getId());
        communityJoinRequestRepository.deleteByCommunity(community);
        communityMemberRepository.deleteByCommunity(community);

        communityRepository.delete(community);
    }

    @CacheEvict(value = {"communities_public", "teams_public"}, allEntries = true)
    @Transactional
    public void deleteCommunity(UUID userId, String identifier, boolean deleteTeam) {
        Community community = findCommunityByIdentifier(identifier);
        deleteCommunity(userId, community.getId(), deleteTeam);
    }

    @CacheEvict(value = "communities_public", allEntries = true)
    @Transactional
    public void archiveCommunity(UUID userId, UUID communityId) {
        User user = userFinder.findProfileByUserId(userId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (!community.getOwner().getId().equals(user.getId())) {
            throw new NotFoundException("Community not found");
        }

        community.setStatus(CommunityStatus.ARCHIVED);
        communityRepository.save(community);
    }

    @CacheEvict(value = "communities_public", allEntries = true)
    @Transactional
    public void archiveCommunity(UUID userId, String identifier) {
        Community community = findCommunityByIdentifier(identifier);
        archiveCommunity(userId, community.getId());
    }

    @Transactional(readOnly = true)
    public Page<CommunityResponse> listCommunities(UUID userId, String name, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        String trimmedName = name == null ? "" : name.trim();
        Page<Community> communities = trimmedName.isEmpty()
                ? communityRepository.findAllByStatus(CommunityStatus.ACTIVE, pageable)
                : communityRepository.findAllByStatusAndNameContainingIgnoreCase(CommunityStatus.ACTIVE, trimmedName, pageable);

        return communities.map(CommunityMapper::toResponse);
    }

    /** Mesma ideia de TeamService.listingMyTeams, mas via community_members: só as
     * comunidades onde o usuário tem vínculo ACTIVE (dono ou membro comum). */
    @Transactional(readOnly = true)
    public Page<CommunityResponse> listMyCommunities(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        return communityMemberRepository.findByUserAndStatus(user, CommunityMemberStatus.ACTIVE, pageable)
                .map(member -> CommunityMapper.toResponse(member.getCommunity()));
    }

    /**
     * Comunidade vinculada a um Team, se existir. Fica aqui (e não em TeamResponse)
     * porque o vínculo é guardado do lado de communities (communities.team_id) — incluir
     * no TeamResponse obrigaria um lookup extra em todos os pontos que montam um Team.
     */
    @Transactional(readOnly = true)
    public CommunityResponse detailCommunityByTeam(String teamIdentifier) {
        UUID teamId;
        try {
            teamId = UUID.fromString(teamIdentifier);
        } catch (IllegalArgumentException e) {
            teamId = teamRepository.findBySlug(teamIdentifier)
                    .map(com.hokyozu.kyofuse.teams.entity.Team::getId)
                    .orElseThrow(() -> new NotFoundException("Community not found"));
        }

        Community community = communityRepository.findByTeamId(teamId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new NotFoundException("Community not found");
        }

        return CommunityMapper.toResponse(community);
    }

    @Transactional(readOnly = true)
    public CommunityResponse detailCommunityByTeam(UUID teamId) {
        return detailCommunityByTeam(teamId.toString());
    }

    /**
     * Comunidades de um usuário qualquer, para exibir no perfil dele. Participação em
     * comunidade é informação pública (mesmo tratamento da lista de membros de um
     * Team), então não passa pela Política de Autorização Social.
     */
    @Transactional(readOnly = true)
    public Page<CommunityResponse> listUserCommunities(UUID viewerId, UUID userId, Pageable pageable) {
        User viewer = userFinder.findProfileByUserId(viewerId);
        userChecker.checkActive(viewer);

        return communityMemberRepository.findByUserIdAndStatus(userId, CommunityMemberStatus.ACTIVE, pageable)
                .map(member -> CommunityMapper.toResponse(member.getCommunity()));
    }

    @CacheEvict(value = {"communities_public", "teams_public"}, allEntries = true)
    @Transactional
    public CommunityResponse createCommunityFromTeam(UUID userId, String teamIdentifier) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        teamChecker.checkInactive(team);

        boolean isTeamOwner = team.getOwner().getId().equals(user.getId());
        boolean isTeamManager = Optional.ofNullable(teamMemberRepository.findByTeamAndUser(team, user))
                .filter(tm -> tm.getStatus() == TeamMemberStatus.ACTIVE && tm.getMemberType() == TeamMemberType.MANAGER)
                .isPresent();
        if (!isTeamOwner && !isTeamManager) {
            throw new ForbiddenException("Apenas o dono ou gerentes do time podem criar uma comunidade para o time.");
        }

        if (communityRepository.findByTeamId(team.getId()).isPresent()) {
            throw new ConflictException("Este time já possui uma comunidade vinculada.");
        }

        User owner = team.getOwner();
        String slug = resolveAvailableSlug(team.getSlug());
        Community community = CommunityMapper.toEntityTeamCommunity(owner, team, slug);
        communityRepository.save(community);
        communityMemberRepository.save(CommunityMemberMapper.toOwnerEntity(owner, community));
        conversationService.createCommunityConversation(community, owner);

        List<TeamMember> activeMembers = teamMemberRepository.findByTeamAndStatus(team, TeamMemberStatus.ACTIVE);
        for (TeamMember tm : activeMembers) {
            if (tm.getUser().getId().equals(owner.getId())) {
                continue;
            }
            if (tm.getMemberType() == TeamMemberType.MANAGER) {
                communityMemberRepository.save(CommunityMemberMapper.toAdminEntity(tm.getUser(), community));
            } else {
                communityMemberRepository.save(CommunityMemberMapper.toEntity(tm.getUser(), community));
            }
        }

        return CommunityMapper.toResponse(community);
    }

    @CacheEvict(value = {"communities_public", "teams_public"}, allEntries = true)
    @Transactional
    public CommunityResponse attachTeamAndCommunity(UUID userId, String teamIdentifier, String communityIdentifier) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        teamChecker.checkInactive(team);

        Community community = findCommunityByIdentifier(communityIdentifier);

        if (communityRepository.findByTeamId(team.getId()).isPresent()) {
            throw new ConflictException("Este time já possui uma comunidade vinculada.");
        }
        if (community.getTeam() != null) {
            throw new ConflictException("Esta comunidade já possui um time vinculado.");
        }

        boolean isTeamHead = team.getOwner().getId().equals(user.getId()) ||
                Optional.ofNullable(teamMemberRepository.findByTeamAndUser(team, user))
                        .filter(tm -> tm.getStatus() == TeamMemberStatus.ACTIVE && tm.getMemberType() == TeamMemberType.MANAGER)
                        .isPresent();

        boolean isCommunityHead = community.getOwner().getId().equals(user.getId()) ||
                communityMemberRepository.findByCommunityAndUser(community, user)
                        .filter(cm -> cm.getStatus() == CommunityMemberStatus.ACTIVE && cm.getRole() == CommunityMemberRole.ADMIN)
                        .isPresent();

        if (!isTeamHead || !isCommunityHead) {
            throw new ForbiddenException("Você precisa ter privilégios administrativos no time e na comunidade para vinculá-los.");
        }

        community.setTeam(team);
        community.setUpdatedAt(Instant.now());
        communityRepository.save(community);

        User teamOwner = team.getOwner();
        List<TeamMember> activeTeamMembers = teamMemberRepository.findByTeamAndStatus(team, TeamMemberStatus.ACTIVE);
        List<User> teamHeads = new ArrayList<>();
        teamHeads.add(teamOwner);
        for (TeamMember tm : activeTeamMembers) {
            if (tm.getMemberType() == TeamMemberType.MANAGER && !tm.getUser().getId().equals(teamOwner.getId())) {
                teamHeads.add(tm.getUser());
            }
        }

        User communityOwner = community.getOwner();
        List<CommunityMember> activeCommunityMembers = communityMemberRepository.findByCommunityAndStatus(community, CommunityMemberStatus.ACTIVE);
        List<User> communityHeads = new ArrayList<>();
        communityHeads.add(communityOwner);
        for (CommunityMember cm : activeCommunityMembers) {
            if (cm.getRole() == CommunityMemberRole.ADMIN && !cm.getUser().getId().equals(communityOwner.getId())) {
                communityHeads.add(cm.getUser());
            }
        }

        // Cabeças do time viram ADMIN na comunidade
        for (User head : teamHeads) {
            CommunityMember cm = communityMemberRepository.findByCommunityAndUser(community, head).orElse(null);
            if (cm == null) {
                communityMemberRepository.save(CommunityMemberMapper.toAdminEntity(head, community));
            } else {
                cm.setStatus(CommunityMemberStatus.ACTIVE);
                cm.setRole(CommunityMemberRole.ADMIN);
                cm.setLeftAt(null);
                cm.setUpdatedAt(Instant.now());
                communityMemberRepository.save(cm);
            }
        }

        // Cabeças da comunidade viram MANAGER no time
        for (User head : communityHeads) {
            TeamMember tm = teamMemberRepository.findByTeamAndUser(team, head);
            if (tm == null) {
                teamMemberRepository.save(TeamMemberMapper.toManagerEntity(head, team));
            } else {
                tm.setStatus(TeamMemberStatus.ACTIVE);
                tm.setMemberType(TeamMemberType.MANAGER);
                tm.setLeftAt(null);
                tm.setAssignmentDueAt(null);
                tm.setUpdatedAt(Instant.now());
                teamMemberRepository.save(tm);
            }
        }

        // Membros regulares do time entram como MEMBER na comunidade (se não forem cabeça)
        for (TeamMember tm : activeTeamMembers) {
            if (tm.getMemberType() != TeamMemberType.MANAGER && !tm.getUser().getId().equals(teamOwner.getId())) {
                CommunityMember cm = communityMemberRepository.findByCommunityAndUser(community, tm.getUser()).orElse(null);
                if (cm == null) {
                    communityMemberRepository.save(CommunityMemberMapper.toEntity(tm.getUser(), community));
                } else if (cm.getStatus() != CommunityMemberStatus.ACTIVE && cm.getStatus() != CommunityMemberStatus.BANNED) {
                    cm.setStatus(CommunityMemberStatus.ACTIVE);
                    cm.setRole(CommunityMemberRole.MEMBER);
                    cm.setLeftAt(null);
                    cm.setUpdatedAt(Instant.now());
                    communityMemberRepository.save(cm);
                }
            }
        }

        return CommunityMapper.toResponse(community);
    }

    @Transactional(readOnly = true)
    public List<CommunityResponse> listAvailableCommunitiesForTeam(UUID userId, String teamIdentifier) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        teamChecker.checkInactive(team);

        if (communityRepository.findByTeamId(team.getId()).isPresent()) {
            return List.of();
        }

        boolean isTeamHead = team.getOwner().getId().equals(user.getId()) ||
                Optional.ofNullable(teamMemberRepository.findByTeamAndUser(team, user))
                        .filter(tm -> tm.getStatus() == TeamMemberStatus.ACTIVE && tm.getMemberType() == TeamMemberType.MANAGER)
                        .isPresent();
        if (!isTeamHead) {
            throw new ForbiddenException("Apenas administradores do time podem consultar comunidades para vincular.");
        }

        return communityRepository.findAvailableForTeam(userId).stream()
                .map(CommunityMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> listAvailableTeamsForCommunity(UUID userId, String communityIdentifier) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = findCommunityByIdentifier(communityIdentifier);

        if (community.getTeam() != null) {
            return List.of();
        }

        boolean isCommunityHead = community.getOwner().getId().equals(user.getId()) ||
                communityMemberRepository.findByCommunityAndUser(community, user)
                        .filter(cm -> cm.getStatus() == CommunityMemberStatus.ACTIVE && cm.getRole() == CommunityMemberRole.ADMIN)
                        .isPresent();
        if (!isCommunityHead) {
            throw new ForbiddenException("Apenas administradores da comunidade podem consultar times para vincular.");
        }

        return teamRepository.findAvailableForCommunity(userId).stream()
                .map(t -> TeamMapper.toResponse(t, teamRequiredRoleRepository.findByTeamId(t.getId())))
                .toList();
    }

    @CacheEvict(value = {"communities_public", "teams_public"}, allEntries = true)
    @Transactional
    public void detachTeamCommunityByTeam(UUID userId, String teamIdentifier) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Team team = teamFinder.findTeamByIdentifier(teamIdentifier);
        Community community = communityRepository.findByTeamId(team.getId())
                .orElseThrow(() -> new BadRequestException("O time não possui comunidade vinculada."));

        if (!isUserHeadOfTeam(team, user) && !isUserHeadOfCommunity(community, user)) {
            throw new ForbiddenException("Apenas os donos ou administradores/gerentes podem desvincular o time e a comunidade.");
        }

        community.setTeam(null);
        community.setUpdatedAt(Instant.now());
        communityRepository.save(community);
    }

    @CacheEvict(value = {"communities_public", "teams_public"}, allEntries = true)
    @Transactional
    public void detachTeamCommunityByCommunity(UUID userId, String communityIdentifier) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = findCommunityByIdentifier(communityIdentifier);
        if (community.getTeam() == null) {
            throw new BadRequestException("A comunidade não possui time vinculado.");
        }
        Team team = community.getTeam();

        if (!isUserHeadOfTeam(team, user) && !isUserHeadOfCommunity(community, user)) {
            throw new ForbiddenException("Apenas os donos ou administradores/gerentes podem desvincular o time e a comunidade.");
        }

        community.setTeam(null);
        community.setUpdatedAt(Instant.now());
        communityRepository.save(community);
    }

    public boolean isUserHeadOfTeam(Team team, User user) {
        if (team.getOwner().getId().equals(user.getId())) {
            return true;
        }
        return teamMemberRepository.findByTeamAndStatus(team, TeamMemberStatus.ACTIVE).stream()
                .anyMatch(tm -> tm.getUser().getId().equals(user.getId()) && tm.getMemberType() == TeamMemberType.MANAGER);
    }

    public boolean isUserHeadOfCommunity(Community community, User user) {
        if (community.getOwner().getId().equals(user.getId())) {
            return true;
        }
        return communityMemberRepository.findByCommunityAndStatus(community, CommunityMemberStatus.ACTIVE).stream()
                .anyMatch(cm -> cm.getUser().getId().equals(user.getId()) && cm.getRole() == CommunityMemberRole.ADMIN);
    }
}
