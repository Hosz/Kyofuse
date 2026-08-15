package com.hokyozu.kyofuse.communities.validator;

import com.hokyozu.kyofuse.communities.dto.request.CommunityRequest;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validações sintáticas (obrigatoriedade, tamanho, formato) ficam nas anotações
 * de Bean Validation em CommunityRequest. Aqui só entra o que depende do banco.
 */
@Component
@RequiredArgsConstructor
public class CommunityCreationValidator {

    private final CommunityRepository communityRepository;

    public void validate(@Valid CommunityRequest request) {
        if (communityRepository.existsBySlug(request.communitySlug())) {
            throw new ConflictException("Slug já está em uso.");
        }
    }
}
