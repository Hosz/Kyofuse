package com.hokyozu.kyofuse.teams.repository;

import com.hokyozu.kyofuse.teams.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {
}
