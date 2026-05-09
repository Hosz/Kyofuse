package com.hokyozu.kyofuse.profiles.repository;

import com.hokyozu.kyofuse.profiles.entity.GamerProfileFavoriteMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GamerProfileFavoriteMapRepository extends JpaRepository<GamerProfileFavoriteMap, UUID> {

    List<GamerProfileFavoriteMap> findByProfile_Id(UUID profileId);

    void deleteByProfile_Id(UUID profileId);
}
