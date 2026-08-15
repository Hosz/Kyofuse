package com.hokyozu.kyofuse.communities.validator;

import com.hokyozu.kyofuse.communities.dto.request.UpdateCommunityRequest;
import com.hokyozu.kyofuse.communities.entity.Community;
import com.hokyozu.kyofuse.communities.repository.CommunityRepository;
import com.hokyozu.kyofuse.shared.exception.ConflictException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validações sintáticas (tamanho, formato) ficam nas anotações de Bean Validation
 * em UpdateCommunityRequest. Aqui só entra o que depende do banco.
 */
@Component
@RequiredArgsConstructor
public class CommunityEditValidator {

    private final CommunityRepository communityRepository;

    public void validate(@Valid UpdateCommunityRequest request, Community community) {
        boolean slugChanged = request.communitySlug() != null
                && !request.communitySlug().equals(community.getSlug());

        if (slugChanged && communityRepository.existsBySlugAndIdNot(request.communitySlug(), community.getId())) {
            throw new ConflictException("Slug já está em uso.");
        }
    }
}
