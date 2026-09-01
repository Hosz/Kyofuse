UPDATE user_privacy_settings
SET message_permission = 'EVERYONE'
WHERE message_permission IN ('FRIENDS', 'FOLLOWERS');
