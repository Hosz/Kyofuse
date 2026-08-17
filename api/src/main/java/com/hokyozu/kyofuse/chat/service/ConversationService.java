package com.hokyozu.kyofuse.chat.service;

import com.hokyozu.kyofuse.chat.dto.request.ConversationRequest;
import com.hokyozu.kyofuse.chat.dto.response.ConversationResponse;
import com.hokyozu.kyofuse.chat.entity.Conversation;
import com.hokyozu.kyofuse.chat.entity.ConversationMember;
import com.hokyozu.kyofuse.chat.enums.ConversationMemberStatus;
import com.hokyozu.kyofuse.chat.enums.ConversationType;
import com.hokyozu.kyofuse.chat.enums.DirectConversationStatus;
import com.hokyozu.kyofuse.chat.mapper.ConversationMapper;
import com.hokyozu.kyofuse.chat.mapper.ConversationMemberMapper;
import com.hokyozu.kyofuse.chat.repository.ConversationMemberRepository;
import com.hokyozu.kyofuse.chat.repository.ConversationRepository;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.entity.CommunityMember;
import com.hokyozu.kyofuse.communities.enums.CommunityMemberStatus;
import com.hokyozu.kyofuse.communities.enums.CommunityStatus;
import com.hokyozu.kyofuse.communities.repository.CommunityMemberRepository;
import com.hokyozu.kyofuse.relationships.permission.service.message.MessagePermissionService;
import com.hokyozu.kyofuse.relationships.shared.validator.BlockValidator;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import com.hokyozu.kyofuse.shared.exception.ForbiddenException;
import com.hokyozu.kyofuse.shared.exception.NotFoundException;
import com.hokyozu.kyofuse.users.entity.User;
import com.hokyozu.kyofuse.users.finder.UserFinder;
import com.hokyozu.kyofuse.users.service.UserChecker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final UserFinder userFinder;
    private final UserChecker userChecker;

    private final MessagePermissionService messagePermissionService;
    private final BlockValidator blockValidator;

    private final ConversationRepository conversationRepository;
    private final CommunityMemberRepository communityMemberRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    @Transactional
    public ConversationResponse createConversation(@Valid ConversationRequest request, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        for (UUID participantId : request.participantIds()) {
            User participant = userFinder.findProfileByUserId(participantId);
            userChecker.checkActive(participant);
        }

        if (request.participantIds().size() >= 2) {
            return createGroupConversation(request, user);
        }

        return createOrGetDirectConversation(user, request.participantIds().getFirst());
    }

    private ConversationResponse createGroupConversation(ConversationRequest request, User user) {
        Conversation conversation = ConversationMapper.toEntityGroup(request, user);
        conversationRepository.save(conversation);

        ConversationMember conversationOwner = ConversationMemberMapper.toGroupAdmin(conversation, user);
        conversationMemberRepository.save(conversationOwner);

        // participantIds representa "os outros" — se o próprio criador vier na lista
        // (por engano ou repetido), ele é ignorado aqui pra não violar a UNIQUE de
        // conversation_members tentando salvar duas linhas pro mesmo usuário.
        Set<UUID> otherParticipantIds = new LinkedHashSet<>(request.participantIds());
        otherParticipantIds.remove(user.getId());

        for (UUID participantId : otherParticipantIds) {
            User participant = userFinder.findProfileByUserId(participantId);
            blockValidator.validate(user, participant);

            ConversationMember conversationMember = ConversationMemberMapper.toGroupMember(conversation, participant);
            conversationMemberRepository.save(conversationMember);
        }

        return ConversationMapper.toResponse(conversation);
    }

    private ConversationResponse createOrGetDirectConversation(User user, UUID secondParticipantId) {
        User secondParticipant = userFinder.findProfileByUserId(secondParticipantId);

        // menor UUID (comparado como String, não com UUID.compareTo — ver nota no
        // ConversationMapper) sempre em directUserOne, pra bater com a CHECK da
        // migration e garantir no máximo uma conversa DIRECT por par de usuários.
        boolean userIsSmaller = user.getId().toString().compareTo(secondParticipant.getId().toString()) < 0;
        User directUserOne = userIsSmaller ? user : secondParticipant;
        User directUserTwo = userIsSmaller ? secondParticipant : user;

        Conversation existing = conversationRepository
                .findByDirectUserOneAndDirectUserTwo(directUserOne, directUserTwo)
                .orElse(null);

        if (existing != null) {
            return ConversationMapper.toResponse(existing);
        }

        boolean requiresApproval = messagePermissionService.requiresApprovalForFirstMessage(user, secondParticipant);
        DirectConversationStatus status = requiresApproval
                ? DirectConversationStatus.PENDING
                : DirectConversationStatus.ACCEPTED;

        Conversation conversation = ConversationMapper.toEntityDirect(user, directUserOne, directUserTwo, status);
        conversationRepository.save(conversation);
        return ConversationMapper.toResponse(conversation);
    }

    /**
     * Criada automaticamente pelo TeamService/CommunityService junto da Community —
     * não é exposta em nenhum controller. Não cria ConversationMember: o acesso ao
     * chat de uma COMMUNITY é derivado inteiramente de community_members.status =
     * ACTIVE, pra não duplicar a mesma fonte de verdade em duas tabelas.
     */
    @Transactional
    public void createCommunityConversation(Community community, User user) {
        if (conversationRepository.findByCommunity(community).isPresent()) {
            throw new ConflictException("Conversation for this community already exists.");
        }

        Conversation conversation = ConversationMapper.toEntityCommunity(community, user);
        conversationRepository.save(conversation);
    }

    /**
     * O destinatário da primeira mensagem (quem não é created_by) aceita uma conversa
     * DIRECT que nasceu PENDING — ver doc.md 4.6. A partir daqui, ambos os lados podem
     * trocar mensagens livremente, sem necessidade de nova aprovação.
     */
    @Transactional
    public ConversationResponse acceptDirectConversation(UUID conversationId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Conversation conversation = validatePendingDirectConversationRecipient(conversationId, user);

        conversation.setDirectMessageStatus(DirectConversationStatus.ACCEPTED);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return ConversationMapper.toResponse(conversation);
    }

    /**
     * O destinatário recusa a solicitação. A conversa e a primeira mensagem continuam
     * armazenadas (histórico permanente — doc.md 4.6), mas nenhuma mensagem nova pode
     * ser enviada enquanto o status permanecer DECLINED.
     */
    @Transactional
    public ConversationResponse declineDirectConversation(UUID conversationId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Conversation conversation = validatePendingDirectConversationRecipient(conversationId, user);

        conversation.setDirectMessageStatus(DirectConversationStatus.DECLINED);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return ConversationMapper.toResponse(conversation);
    }

    /**
     * Revoga uma conversa DIRECT que estava ACCEPTED, voltando pro estado DECLINED —
     * nenhum novo lado poderá mandar mensagem enquanto ficar assim (mesma regra de
     * quem recusa, ver doc.md 4.6). Diferente de aceitar/recusar um PENDING, aqui não
     * existe mais distinção de "destinatário": depois de aceita, qualquer um dos dois
     * participantes pode decidir encerrar a permissão, não só quem recebeu a 1ª mensagem.
     */
    @Transactional
    public ConversationResponse revokeDirectConversationPermission(UUID conversationId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Conversation conversation = getDirectConversationForParticipant(conversationId, user);

        if (conversation.getDirectMessageStatus() != DirectConversationStatus.ACCEPTED) {
            throw new BadRequestException("This conversation is not currently accepted.");
        }

        conversation.setDirectMessageStatus(DirectConversationStatus.DECLINED);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        return ConversationMapper.toResponse(conversation);
    }

    private Conversation validatePendingDirectConversationRecipient(UUID conversationId, User user) {
        Conversation conversation = getDirectConversationForParticipant(conversationId, user);

        if (conversation.getDirectMessageStatus() != DirectConversationStatus.PENDING) {
            throw new BadRequestException("This conversation is not pending approval.");
        }

        // created_by é quem mandou a primeira mensagem (o solicitante); só o outro
        // participante — o destinatário — pode aceitar ou recusar a própria solicitação.
        if (conversation.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the recipient of the first message can respond to this request.");
        }

        return conversation;
    }

    private Conversation getDirectConversationForParticipant(UUID conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (conversation.getType() != ConversationType.DIRECT) {
            throw new BadRequestException("Only DIRECT conversations support this action.");
        }

        boolean isParticipant = conversation.getDirectUserOne().getId().equals(user.getId())
                || conversation.getDirectUserTwo().getId().equals(user.getId());
        if (!isParticipant) {
            throw new ForbiddenException("User is not a participant in this conversation.");
        }

        return conversation;
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> listDirectConversations(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<Conversation> conversations = conversationRepository.findAllByTypeAndDirectUser(ConversationType.DIRECT, user, pageable);

        return conversations.map(ConversationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> listCommunityConversations(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<CommunityMember> memberships = communityMemberRepository
                .findByUserAndStatus(user, CommunityMemberStatus.ACTIVE, pageable);

        List<ConversationResponse> responses = memberships.getContent().stream()
                .map(CommunityMember::getCommunity)
                .filter(community -> community.getStatus() != CommunityStatus.ARCHIVED)
                .map(community -> conversationRepository.findByCommunity(community)
                        .orElseThrow(() -> new NotFoundException("Conversation for this community not found.")))
                .map(ConversationMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, memberships.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> listGroupConversations(UUID userId, Pageable pageable) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Page<ConversationMember> memberships = conversationMemberRepository
                .findByUserAndStatus(user, ConversationMemberStatus.ACTIVE, pageable);

        List<ConversationResponse> responses = memberships.getContent().stream()
                .map(ConversationMember::getConversation)
                .filter(conversation -> conversation.getType() == ConversationType.GROUP)
                .map(ConversationMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, memberships.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ConversationResponse getConversationDetails(UUID conversationId, UUID userId) {
        User user = userFinder.findProfileByUserId(userId);
        userChecker.checkActive(user);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        switch (conversation.getType()) {
            case DIRECT -> {
                boolean isParticipant = conversation.getDirectUserOne().getId().equals(user.getId())
                        || conversation.getDirectUserTwo().getId().equals(user.getId());
                if (!isParticipant) {
                    throw new ForbiddenException("User is not a participant in this conversation.");
                }
            }
            case GROUP -> conversationMemberRepository.findByConversationAndUser(conversation, user)
                    .filter(member -> member.getStatus() == ConversationMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("User is not a participant in this conversation."));
            case COMMUNITY -> communityMemberRepository.findByCommunityAndUser(conversation.getCommunity(), user)
                    .filter(member -> member.getStatus() == CommunityMemberStatus.ACTIVE)
                    .orElseThrow(() -> new ForbiddenException("User is not a member of the community."));
        }

        return ConversationMapper.toResponse(conversation);
    }
}
