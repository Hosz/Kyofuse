package com.hokyozu.kyofuse.infrastructure.security.crypto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Backfill idempotente: cifra o email (e preenche o email_index) de qualquer usuário
 * gravado antes da coluna email_index existir. Usa JdbcTemplate de propósito — o
 * EncryptedEmailConverter da entidade User tentaria decifrar essas linhas antigas (que
 * ainda estão em texto puro) e falharia, então essa leitura/escrita precisa contornar o
 * JPA. Roda a cada start, mas o WHERE email_index IS NULL faz virar um no-op depois da
 * primeira vez.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEncryptionBackfillRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final EmailCipherService emailCipherService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Map<String, Object>> pending = jdbcTemplate.queryForList(
                "SELECT id, email FROM users WHERE email_index IS NULL"
        );

        for (Map<String, Object> row : pending) {
            UUID id = (UUID) row.get("id");
            String plainEmail = (String) row.get("email");

            jdbcTemplate.update(
                    "UPDATE users SET email = ?, email_index = ? WHERE id = ?",
                    emailCipherService.encrypt(plainEmail),
                    emailCipherService.blindIndex(plainEmail),
                    id
            );
        }

        if (!pending.isEmpty()) {
            log.info("Backfill de criptografia de email: {} usuário(s) migrado(s).", pending.size());
        }
    }
}
