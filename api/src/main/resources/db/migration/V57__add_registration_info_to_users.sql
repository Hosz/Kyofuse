ALTER TABLE users
ADD COLUMN registration_country VARCHAR(100),
ADD COLUMN registration_country_code VARCHAR(10),
ADD COLUMN registration_device VARCHAR(150);

-- Backfill registration info for existing users from their earliest user_session or gamer_profiles
UPDATE users u
SET 
    registration_country = COALESCE(
        u.registration_country,
        (
            SELECT s.location 
            FROM user_sessions s 
            WHERE s.user_id = u.id 
            ORDER BY s.created_at ASC 
            LIMIT 1
        ),
        (
            SELECT gp.country 
            FROM gamer_profiles gp 
            WHERE gp.user_id = u.id 
            LIMIT 1
        )
    ),
    registration_device = COALESCE(
        u.registration_device,
        (
            SELECT s.device_name 
            FROM user_sessions s 
            WHERE s.user_id = u.id 
            ORDER BY s.created_at ASC 
            LIMIT 1
        )
    );
