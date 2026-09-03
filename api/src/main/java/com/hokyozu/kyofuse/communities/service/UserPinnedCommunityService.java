package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.UserPinnedCommunity;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.mapper.CommunityMapper;
import com.hokyozu.kyofuse.communities.mapper.UserPinnedCommunityMapper;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.repository.UserPinnedCommunityRepository;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Comunidades que o usuário fixou no feed da página inicial. Fixar não tem relação com
 * ser membro: qualquer comunidade ACTIVE pode ser fixada, e o que o usuário vai enxergar
 * no feed dela continua sujeito às regras de visibilidade de posts (ver doc.md 10.5).
 */
@Service
@RequiredArgsConstructor
public class UserPinnedCommunityService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;

    private final CommunityRepository communityRepository;
    private final UserPinnedCommunityRepository userPinnedCommunityRepository;

    @Transactional(readOnly = true)
    public List<CommunityResponse> listPinnedCommunities(UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        return userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId).stream()
                .map(UserPinnedCommunity::getCommunity)
                .filter(community -> community.getStatus() != CommunityStatus.ARCHIVED)
                .map(CommunityMapper::toResponse)
                .toList();
    }

    @Transactional
    public CommunityResponse pinCommunity(UUID userId, UUID communityId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new NotFoundException("Community not found");
        }

        if (userPinnedCommunityRepository.existsByUserIdAndCommunityId(userId, communityId)) {
            throw new ConflictException("Community is already pinned");
        }

        // Nova fixada entra no fim da barra.
        int nextPosition = userPinnedCommunityRepository.countByUserId(userId);

        userPinnedCommunityRepository.save(
                UserPinnedCommunityMapper.toEntity(user, community, nextPosition)
        );

        return CommunityMapper.toResponse(community);
    }

    @Transactional
    public void unpinCommunity(UUID userId, UUID communityId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        UserPinnedCommunity pinned = userPinnedCommunityRepository
                .findByUserIdAndCommunityId(userId, communityId)
                .orElseThrow(() -> new NotFoundException("Community is not pinned"));

        userPinnedCommunityRepository.delete(pinned);

        // Sem renumerar, as posições ficariam com buracos e um reorder seguinte teria
        // que lidar com índices que não batem com a lista exibida.
        reindex(userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId));
    }

    /**
     * Reordena as comunidades fixadas. communityIds precisa conter exatamente as que o
     * usuário tem fixadas — assim uma lista desatualizada (outra aba fixou ou desafixou
     * algo no meio tempo) é recusada em vez de gravar uma ordem parcial.
     */
    @Transactional
    public List<CommunityResponse> reorderPinnedCommunities(UUID userId, List<UUID> communityIds) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        List<UserPinnedCommunity> pinned = userPinnedCommunityRepository.findByUserIdOrderByPositionAsc(userId);

        Set<UUID> currentIds = pinned.stream()
                .map(item -> item.getCommunity().getId())
                .collect(Collectors.toSet());
        Set<UUID> requestedIds = Set.copyOf(communityIds);

        if (communityIds.size() != requestedIds.size() || !currentIds.equals(requestedIds)) {
            throw new BadRequestException("The list must contain exactly the pinned communities, without repeats.");
        }

        Map<UUID, UserPinnedCommunity> byCommunityId = pinned.stream()
                .collect(Collectors.toMap(item -> item.getCommunity().getId(), item -> item));

        List<UserPinnedCommunity> reordered = communityIds.stream().map(byCommunityId::get).toList();
        reindex(reordered);

        return reordered.stream()
                .map(item -> CommunityMapper.toResponse(item.getCommunity()))
                .toList();
    }

    private void reindex(List<UserPinnedCommunity> pinned) {
        for (int index = 0; index < pinned.size(); index++) {
            pinned.get(index).setPosition(index);
        }
        userPinnedCommunityRepository.saveAll(pinned);
    }
}
