package com.hokyozu.kyofuse.profiles.repository;

import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import com.hokyozu.kyofuse.teams.entity.TeamRequiredRole;
import com.hokyozu.kyofuse.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GamerProfileFavoriteMapRepository extends JpaRepository<GamerProfileFavoriteMap, UUID> {

    List<GamerProfileFavoriteMap> findByProfile_Id(UUID profileId);

    void deleteByProfile_Id(UUID profileId);

    List<GamerProfileFavoriteMap> findByProfile_IdIn(List<UUID> profileIds);
}
