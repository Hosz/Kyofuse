package com.hokyozu.kyofuse.relationships.block.mapper;

import com.hokyozu.kyofuse.relationships.block.dto.response.UserBlockResponse;
import com.hokyozu.kyofuse.profiles.entity.GamerProfile;
import com.hokyozu.kyofuse.relationships.block.entity.UserBlock;
import com.hokyozu.kyofuse.users.entity.User;

import java.time.Instant;

public class UserBlockMapper {
    public static UserBlock toEntity(User user, User userBlocked) {
        return UserBlock.builder()
                .blocker(user)
                .blocked(userBlocked)
                .createdAt(Instant.now())
                .build();
    }

    public static UserBlockResponse toResponse(UserBlock block) {
        return toResponse(block, null);
    }

    /**
     * blockedProfile é opcional: a listagem de bloqueados passa o perfil para exibir
     * apelido e foto de quem foi bloqueado; quem só confirma a ação (bloquear) chama a
     * sobrecarga sem ele e esses campos saem nulos.
     */
    public static UserBlockResponse toResponse(UserBlock block, GamerProfile blockedProfile) {
        return new UserBlockResponse(
                "Blocked",
                block.getBlocker().getId(),
                block.getBlocker().getUsername(),
                block.getBlocked().getId(),
                block.getBlocked().getUsername(),
                blockedProfile != null ? blockedProfile.getNickname() : null,
                blockedProfile != null ? blockedProfile.getAvatarUrl() : null,
                block.getCreatedAt()
        );
    }
}
