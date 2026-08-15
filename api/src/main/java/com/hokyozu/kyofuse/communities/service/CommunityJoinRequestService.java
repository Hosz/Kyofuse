package com.hokyozu.kyofuse.communities.service;

import com.hokyozu.kyofuse.communities.dto.response.CommunityJoinRequestResponse;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityJoinRequest;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityVisibility;
import com.hokyozu.kyofuse.communities.mapper.CommunityJoinRequestMapper;
import com.hokyozu.kyofuse.communities.repository.CommunityJoinRequestRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityJoinRequestService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;
    private final BlockValidator blockValidator;

    private final CommunityMemberService communityMemberService;

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final CommunityJoinRequestRepository communityJoinRequestRepository;

    @Transactional
    public CommunityJoinRequestResponse requestToJoinCommunity(UUID userId, UUID communityId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new NotFoundException("Community not found"));

        if (community.getStatus() == CommunityStatus.ARCHIVED) {
            throw new BadRequestException("Cannot request to join an archived community");
        }

        if (community.getVisibility() != CommunityVisibility.PRIVATE) {
            throw new BadRequestException("This community is public, join it directly instead of requesting");
        }

        blockValidator.validate(user, community.getOwner());

        communityMemberRepository.findByUserIdAndCommunityId(userId, communityId).ifPresent(member -> {
            switch (member.getStatus()) {
                case ACTIVE -> throw new BadRequestException("User is already a member of the community");
                case BANNED -> throw new ForbiddenException("User is banned from this community");
                default -> { /* LEFT, REMOVED or KICKED: pode solicitar entrada novamente */ }
            }
        });

        if (communityJoinRequestRepository.existsByCommunityIdAndRequesterId(communityId, userId)) {
            throw new ConflictException("A join request for this community is already pending");
        }

        CommunityJoinRequest communityJoinRequest = CommunityJoinRequestMapper.toEntity(user, community);
        communityJoinRequestRepository.save(communityJoinRequest);

        return CommunityJoinRequestMapper.toResponse(communityJoinRequest);
    }

    @Transactional
    public void approveJoinRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        CommunityJoinRequest communityJoinRequest = communityJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Join request not found"));

        Community community = communityJoinRequest.getCommunity();
        if (!communityMemberService.isOwnerOrStaff(user, community)) {
            throw new ForbiddenException("Only the community owner, an admin or a moderator can approve join requests");
        }

        blockValidator.validate(user, communityJoinRequest.getRequester());

        communityMemberService.addMember(communityJoinRequest.getRequester(), community);
        communityJoinRequestRepository.delete(communityJoinRequest);
    }

    @Transactional
    public void rejectJoinRequest(UUID userId, UUID requestId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        CommunityJoinRequest communityJoinRequest = communityJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Join request not found"));

        if (!communityMemberService.isOwnerOrStaff(user, communityJoinRequest.getCommunity())) {
            throw new ForbiddenException("Only the community owner, an admin or a moderator can reject join requests");
        }

        communityJoinRequestRepository.delete(communityJoinRequest);
    }
}
