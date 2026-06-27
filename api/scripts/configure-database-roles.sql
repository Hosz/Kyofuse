\set ON_ERROR_STOP on

\if :{?app_password}
\else
  \echo 'Missing required variable: app_password'
  \quit
\endif

\if :{?migration_password}
\else
  \echo 'Missing required variable: migration_password'
  \quit
\endif

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'kyofuse_migration') THEN
        CREATE ROLE kyofuse_migration LOGIN;
    END IF;

    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'kyofuse_app') THEN
        CREATE ROLE kyofuse_app LOGIN;
    END IF;
END
$$;

SELECT format('ALTER ROLE kyofuse_migration PASSWORD %L', :'migration_password') \gexec
SELECT format('ALTER ROLE kyofuse_app PASSWORD %L', :'app_password') \gexec

REVOKE CREATE ON SCHEMA public FROM PUBLIC;

ALTER SCHEMA public OWNER TO kyofuse_migration;

SELECT format(
    'ALTER TABLE %I.%I OWNER TO kyofuse_migration',
    schemaname,
    tablename
)
FROM pg_tables
WHERE schemaname = 'public'
\gexec

SELECT format(
    'ALTER SEQUENCE %I.%I OWNER TO kyofuse_migration',
    sequence_schema,
    sequence_name
)
FROM information_schema.sequences
WHERE sequence_schema = 'public'
\gexec

GRANT CONNECT ON DATABASE kyofuse TO kyofuse_app;
GRANT USAGE ON SCHEMA public TO kyofuse_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO kyofuse_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO kyofuse_app;

ALTER DEFAULT PRIVILEGES FOR ROLE kyofuse_migration IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO kyofuse_app;

ALTER DEFAULT PRIVILEGES FOR ROLE kyofuse_migration IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO kyofuse_app;
