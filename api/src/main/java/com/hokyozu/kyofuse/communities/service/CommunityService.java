package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.chat.service.ConversationService;
import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.mapper.CommunityMapper;
import com.hokyozu.kyofuse.communities.mapper.CommunityMemberMapper;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.validator.CommunityCreationValidator;
import com.hokyozu.kyofuse.communities.validator.CommunityEditValidator;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.storage.service.ImageProcessingService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.hokyozu.kyofuse.teams.entity.Team;
import com.hokyozu.kyofuse.teams.repository.TeamRepository;
import com.hokyozu.kyofuse.teams.service.TeamChecker;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TeamRepository teamRepository;
    private final TeamChecker teamChecker;

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

    @Transactional(readOnly = true)
    public CommunityResponse detailCommunity(String identifier) {
        Community community;
        try {
            community = communityRepository.findById(java.util.UUID.fromString(identifier))
                    .orElseThrow(() -> new NotFoundException("Community not found"));
        } catch (IllegalArgumentException e) {
            community = communityRepository.findBySlug(identifier)
                    .orElseThrow(() -> new NotFoundException("Community not found"));
        }

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new NotFoundException("Community not found");
        }

        return CommunityMapper.toResponse(community);
    }

    @Transactional
    public void deleteCommunity(UUID userId, UUID communityId) {
        User user = userFinder.findProfileByUserId(userId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (!community.getOwner().getId().equals(user.getId())) {
            throw new NotFoundException("Community not found");
        }

        communityRepository.delete(community);
    }

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
    public CommunityResponse detailCommunityByTeam(UUID teamId) {
        Community community = communityRepository.findByTeamId(teamId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new NotFoundException("Community not found");
        }

        return CommunityMapper.toResponse(community);
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
}
