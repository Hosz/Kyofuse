package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.dto.response.CommunityResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.mapper.CommunityMapper;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.communities.validator.CommunityCreationValidator;
import com.hokyozu.kyofuse.communities.validator.CommunityEditValidator;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    private final CommunityRepository communityRepository;

    @Transactional
    public CommunityResponse createCommunity(@Valid CommunityRequest request, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        communityCreationValidator.validate(request);

        Community community = CommunityMapper.toEntity(request, user);
        communityRepository.save(community);

        return CommunityMapper.toResponse(community);
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
    public CommunityResponse detailCommunity(UUID communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

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
}
