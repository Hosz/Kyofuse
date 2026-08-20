package com.hokyozu.kyofuse.communities.entity;

import com.hokyozu.kyofuse.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_pinned_communities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPinnedCommunity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    /** Ordem na barra de abas do feed, começando em 0 — definida pelo próprio usuário. */
    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
