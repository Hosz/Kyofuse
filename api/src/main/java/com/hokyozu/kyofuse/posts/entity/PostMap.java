package com.hokyozu.kyofuse.posts.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "post_maps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post postId;

    @Column(length = 60, nullable = false)
    private String mapName;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
