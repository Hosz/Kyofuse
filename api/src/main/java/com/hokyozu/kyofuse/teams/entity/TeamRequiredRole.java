package com.hokyozu.kyofuse.teams.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_required_roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequiredRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team teamId;

    @Column(name = "role_name", length = 40, nullable = false)
    private String roleName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
