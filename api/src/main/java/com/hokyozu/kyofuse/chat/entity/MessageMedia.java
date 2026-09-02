package com.hokyozu.kyofuse.chat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "message_media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "file_key", length = 255, nullable = false)
    private String fileKey;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "content_type", length = 50, nullable = false)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
