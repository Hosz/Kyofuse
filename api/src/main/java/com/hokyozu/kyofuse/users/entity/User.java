package com.hokyozu.kyofuse.users.entity;

import com.hokyozu.kyofuse.infrastructure.security.crypto.EncryptedEmailConverter;
import com.hokyozu.kyofuse.infrastructure.security.crypto.EncryptedTotpSecretConverter;
import com.hokyozu.kyofuse.users.enums.UserRole;
import com.hokyozu.kyofuse.users.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name", length = 80, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 120, nullable = false)
    private String lastName;

    @Convert(converter = EncryptedEmailConverter.class)
    @Column(name = "email", length = 500, nullable = false)
    private String email;

    /**
     * HMAC-SHA256 do email normalizado — usado pra busca/unicidade, já que a coluna
     * email guarda um valor cifrado (não comparável por igualdade). Sempre que o email
     * mudar, esse campo precisa ser recalculado junto (EmailCipherService.blindIndex).
     */
    @Column(name = "email_index", length = 64)
    private String emailIndex;

    @Column(name = "username", length = 40, nullable = false)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "status", length =30, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    @Column(name = "deactivated_at")
    private Instant deactivatedAt = null;

    @Builder.Default
    @Column(name = "deletion_scheduled_at")
    private Instant deletionScheduledAt = null;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Convert(converter = EncryptedTotpSecretConverter.class)
    @Column(name = "totp_secret", length = 500)
    private String totpSecret;

    @Column(name = "totp_enabled", nullable = false)
    @Builder.Default
    private boolean totpEnabled = false;

    @Column(name = "totp_confirmed_at")
    private Instant totpConfirmedAt;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "steam_id", length = 30)
    private String steamId;

    @Column(name = "google_id", length = 255)
    private String googleId;

    @Column(name = "has_custom_password", nullable = false)
    @Builder.Default
    private boolean hasCustomPassword = true;
}
