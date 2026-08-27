-- O email passa a ser guardado cifrado (AES-GCM, na aplicação) em vez de texto puro.
-- Como a cifragem usa um IV aleatório, o mesmo email cifrado duas vezes nunca fica
-- igual — por isso a busca/unicidade migra para email_index (HMAC-SHA256 determinístico
-- do email normalizado), e não mais para a própria coluna email.
ALTER TABLE users
    ALTER COLUMN email TYPE VARCHAR(500);

ALTER TABLE users
    ADD COLUMN email_index VARCHAR(64);

ALTER TABLE users
    DROP CONSTRAINT uk_users_email;

ALTER TABLE users
    ADD CONSTRAINT uk_users_email_index UNIQUE (email_index);
