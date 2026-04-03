CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    discord_id VARCHAR(32) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    access_token VARCHAR(255),
    refresh_token VARCHAR(255),
    token_expires_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS guilds (
    discord_id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id VARCHAR(32),
    icon_url VARCHAR(255),
    bot_installed BOOLEAN DEFAULT FALSE,
    welcome_active BOOLEAN DEFAULT FALSE,
    moderation_active BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS guild_settings (
    id BIGSERIAL PRIMARY KEY,
    guild_id VARCHAR(32) NOT NULL REFERENCES guilds(discord_id),
    setting_key VARCHAR(64) NOT NULL,
    setting_value varchar(550),
    UNIQUE (guild_id, setting_key)
);

