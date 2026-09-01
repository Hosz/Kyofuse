ALTER TABLE user_privacy_settings
    DROP COLUMN IF EXISTS posts_visibility,
    DROP COLUMN IF EXISTS likes_visibility,
    DROP COLUMN IF EXISTS reposts_visibility,
    DROP COLUMN IF EXISTS friends_visibility,
    DROP COLUMN IF EXISTS followers_visibility,
    DROP COLUMN IF EXISTS following_visibility,
    DROP COLUMN IF EXISTS follow_permission,
    DROP COLUMN IF EXISTS duo_invite_permission;
