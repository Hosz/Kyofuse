package com.hokyozu.kyofuse.users.entity;

import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(length = 80, nullable = false)
    private String firstName;

    @Column(length = 120, nullable = false)
    private String lastName;

    @Column(length = 160, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(length =30, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    private OffsetDateTime deactivatedAt = null;

    @Builder.Default
    private OffsetDateTime deletionScheduledAt = null;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
