package de.muv1n.muvbot.service;

import de.muv1n.muvbot.api.dto.ChannelDto;
import de.muv1n.muvbot.api.dto.GuildSettingsDto;
import de.muv1n.muvbot.api.dto.RolesDto;
import de.muv1n.muvbot.bot.LinkCommandListener;
import de.muv1n.muvbot.entity.Guild;
import de.muv1n.muvbot.entity.GuildSetting;
import de.muv1n.muvbot.repository.GuildSettingRepository;
import de.muv1n.muvbot.repository.GuildRepository;
import de.muv1n.muvbot.repository.UserRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class GuildService {

    private final JDA jda;
    private final RestTemplate restTemplate;
    private final GuildSettingRepository guildSettingRepository;
    private final GuildRepository guildRepository;
    private final UserRepository userRepository;

    private final Map<String, CacheEntry> guildCache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 60_000;

    private record CacheEntry(long timestamp, List<Guild> guilds) {}

    private final Map<String, UserCacheEntry> userCache = new ConcurrentHashMap<>();

    private record UserCacheEntry(long timestamp, String userId) {}

    public GuildService(JDA jda, GuildSettingRepository guildSettingRepository, GuildRepository guildRepository, UserRepository userRepository, RestTemplate discordRestTemplate) {
        this.jda = jda;
        this.guildSettingRepository = guildSettingRepository;
        this.guildRepository = guildRepository;
        this.userRepository = userRepository;
        this.restTemplate = discordRestTemplate;
    }

    public Guild getGuild(String guildId, String bearerToken) {
        List<Guild> mutualGuilds = getMutualGuilds(bearerToken);
        for (Guild g : mutualGuilds) {
            if (g.getDiscordId().equals(guildId)) {
                return g;
            }
        }
        return null;
    }

    private Guild mapJdaGuildToEntity(net.dv8tion.jda.api.entities.Guild jdaGuild) {
        Guild guild = new Guild();
        guild.setDiscordId(jdaGuild.getId());
        guild.setName(jdaGuild.getName());
        guild.setIconUrl(jdaGuild.getIconUrl());
        guild.setBotInstalled(true);

        guildRepository.findById(jdaGuild.getId()).ifPresent(dbGuild -> guild.setWelcomeActive(dbGuild.isWelcomeActive()));
        return guild;
    }

    public String getUserId(String bearerToken) {
        UserCacheEntry entry = userCache.get(bearerToken);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp() < CACHE_DURATION)) {
            return entry.userId();
        }

        String url = "https://discord.com/api/users/@me";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            );
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("id")) {
                String userId = (String) body.get("id");
                userCache.put(bearerToken, new UserCacheEntry(System.currentTimeMillis(), userId));
                return userId;
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(GuildService.class).error("Failed to fetch user ID", e);
        }
        return null;
    }

    public List<Guild> getMutualGuilds(String bearerToken) {
        // Cache Check
        CacheEntry entry = guildCache.get(bearerToken);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp() < CACHE_DURATION)) {
            return entry.guilds();
        }


        String url = "https://discord.com/api/users/@me/guilds";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            );

            List<Map<String, Object>> discordGuilds = response.getBody();

            List<Guild> mutualGuilds = new ArrayList<>();

            if (discordGuilds == null) return mutualGuilds;

            String userId = getUserId(bearerToken);
            de.muv1n.muvbot.entity.User user = null;
            if (userId != null) {
                user = userRepository.findByDiscordId(userId).orElse(null);
            }

            List<Map<String, Object>> adminGuilds = discordGuilds.stream()
                    .filter(g -> g.get("permissions") != null)
                    .filter(g -> {
                        long p = Long.parseLong(g.get("permissions").toString());
                        return (p & Permission.ADMINISTRATOR.getRawValue()) != 0
                            || (p & Permission.MANAGE_SERVER.getRawValue()) != 0;
                    })
                    .toList();

            List<String> adminGuildIds = adminGuilds.stream()
                    .map(g -> (String) g.get("id"))
                    .toList();

            Map<String, Guild> dbGuildMap = guildRepository.findAllById(adminGuildIds).stream()
                    .collect(Collectors.toMap(Guild::getDiscordId, Function.identity()));

            for (Map<String, Object> g : adminGuilds) {
                Guild guild = new Guild();
                String guildId = (String) g.get("id");
                guild.setDiscordId(guildId);
                guild.setName((String) g.get("name"));
                String icon = (String) g.get("icon");
                if (icon != null) {
                    guild.setIconUrl("https://cdn.discordapp.com/icons/" + guildId + "/" + icon + ".png");
                }
                net.dv8tion.jda.api.entities.Guild jdaGuild = jda.getGuildById(guildId);
                guild.setBotInstalled(jdaGuild != null);

                Guild dbGuild = dbGuildMap.get(guildId);
                if (dbGuild != null) {
                    guild.setWelcomeActive(dbGuild.isWelcomeActive());
                }

                if (user != null && user.getHiddenGuildIds() != null) {
                    guild.setHidden(user.getHiddenGuildIds().contains(guildId));
                }

                mutualGuilds.add(guild);
            }

            // In Cache speichern
            guildCache.put(bearerToken, new CacheEntry(System.currentTimeMillis(), mutualGuilds));

            return mutualGuilds;

        } catch (HttpClientErrorException.TooManyRequests e) {
            System.err.println("Discord Rate Limit hit! Retry after: " + e.getResponseHeaders().getFirst("Retry-After"));
            return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public GuildSettingsDto getSettings(String guildId) {
        List<GuildSetting> settings = guildSettingRepository.findByGuildId(guildId);
        Map<String, String> map = settings.stream()
                .filter(java.util.Objects::nonNull)
                .filter(s -> s.getKey() != null) // toMap() does not allow null keys
                .collect(java.util.stream.Collectors.toMap(
                        GuildSetting::getKey,
                        s -> java.util.Objects.toString(s.getValue(), ""), // toMap() does not allow null values
                        (left, right) -> right // if duplicates exist, keep the latest value
                ));

        // Source flags from Guild Entity
        Guild guild = guildRepository.findById(guildId).orElse(new Guild());

        GuildSettingsDto dto = new GuildSettingsDto();

        // General
        Logger logger = LoggerFactory.getLogger(GuildService.class);
        dto.getGeneral().setName(guild.getName());
        dto.getGeneral().setCommandPrefix(map.getOrDefault("general.prefix", "!"));

        // Welcome
        dto.getWelcome().setEnabled(guild.isWelcomeActive());
        dto.getWelcome().setMessage(map.getOrDefault("welcome.message", "Welcome {user}!"));
        dto.getWelcome().setChannelIds(splitIds(map.getOrDefault("welcome.channelIds", "")));
        dto.getWelcome().setAiEnabled(map.getOrDefault("welcome.ai.message", "false").equalsIgnoreCase("true"));
        dto.getWelcome().setAiPrompt(map.getOrDefault("welcome.ai.prompt", ""));
        dto.getWelcome().setRoleOnJoinEnabled(map.getOrDefault("welcome.role.onjoin.enabled", "false").equalsIgnoreCase("true"));
        dto.getWelcome().setRoleOnJoinIds(splitIds(map.getOrDefault("welcome.role.onjoin.roleIds", "")));

        // Quit
        dto.getQuit().setEnabled(map.getOrDefault("quit.enabled", "false").equalsIgnoreCase("true"));
        dto.getQuit().setMessage(map.getOrDefault("quit.message", "Goodbye {user}!"));
        dto.getQuit().setChannelIds(splitIds(map.getOrDefault("quit.channelIds", "")));

        // MC Whitelist
        dto.getMcWhitelist().setEnabled(map.getOrDefault("mc-whitelist.enabled", "false").equalsIgnoreCase("true"));

        return dto;
    }

    public void updateSettings(String guildId, GuildSettingsDto dto) {
        List<GuildSetting> existing = guildSettingRepository.findByGuildId(guildId);
        Map<String, GuildSetting> map = existing.stream()
                .collect(Collectors.toMap(GuildSetting::getKey, Function.identity()));

        // Sync flags to Guild Entity & Update Name
        Guild guild = guildRepository.findById(guildId).orElse(new Guild());
        guild.setDiscordId(guildId);
        guild.setWelcomeActive(dto.getWelcome().isEnabled());

        // Try to sync Name/Icon from JDA
        net.dv8tion.jda.api.entities.Guild jdaGuild = jda.getGuildById(guildId);
        if (jdaGuild != null) {
            guild.setName(jdaGuild.getName());
            String icon = jdaGuild.getIconId();
            if (icon != null) {
                guild.setIconUrl("https://cdn.discordapp.com/icons/" + guildId + "/" + icon + ".png");
            }
        } else if (guild.getName() == null) {
            guild.setName("Unknown Guild");
        }

        guildRepository.save(guild);

        saveGeneralSettings(guildId, guild.getName(), map, dto);
        saveWelcomeSettings(guildId, guild.getName(), map, dto);
        saveQuitSettings(guildId, guild.getName(), map, dto);
        saveMcWhitelistSettings(guildId, guild.getName(), map, dto);

        // Register or unregister /link slash command based on mc-whitelist toggle
        if (jdaGuild != null) {
            if (dto.getMcWhitelist().isEnabled()) {
                LinkCommandListener.registerCommand(jdaGuild);
            } else {
                LinkCommandListener.unregisterCommand(jdaGuild);
            }
        }
    }

    private void saveSetting(String guildId, String guildName, Map<String, GuildSetting> map, String key, String value) {
        GuildSetting setting = map.getOrDefault(key, new GuildSetting());
        if (setting.getId() == null) {
            setting.setGuildId(guildId);
            setting.setKey(key);
        }
        setting.setName(guildName);
        setting.setValue(value);
        guildSettingRepository.save(setting);
    }

    public List<ChannelDto> getChannels(String guildId) {
        net.dv8tion.jda.api.entities.Guild jdaGuild = jda.getGuildById(guildId);
        if (jdaGuild == null) {
            return new ArrayList<>();
        }

        return jdaGuild.getTextChannels().stream()
                .map(channel -> new ChannelDto(channel.getId(), channel.getName(), "TEXT"))
                .collect(Collectors.toList());
    }

    public List<RolesDto> getRoles(String guildId) {
        net.dv8tion.jda.api.entities.Guild jdaGuild = jda.getGuildById(guildId);
        if (jdaGuild == null) {
            return new ArrayList<>();
        }

        return jdaGuild.getRoles().stream()
                .filter(role -> !role.hasPermission(Permission.ADMINISTRATOR))
                .filter(role -> !role.hasPermission(Permission.MANAGE_SERVER))
                .filter(role -> !role.hasPermission(Permission.MANAGE_CHANNEL))
                .filter(role -> !role.hasPermission(Permission.MANAGE_ROLES))
                .map(role -> new RolesDto(role.getId(), role.getName()))
                .collect(Collectors.toList());
    }

    private void saveGeneralSettings(String guildId, String guildName, Map<String, GuildSetting> map, GuildSettingsDto dto) {
        saveSetting(guildId, guildName, map, "general.prefix", dto.getGeneral().getCommandPrefix());
    }

    private void saveWelcomeSettings(String guildId, String guildName, Map<String, GuildSetting> map, GuildSettingsDto dto) {
        saveSetting(guildId, guildName, map, "welcome.enabled", String.valueOf(dto.getWelcome().isEnabled()));
        saveSetting(guildId, guildName, map, "welcome.message", dto.getWelcome().getMessage());
        saveSetting(guildId, guildName, map, "welcome.channelIds", joinIds(dto.getWelcome().getChannelIds()));
        saveSetting(guildId, guildName, map, "welcome.ai.message", String.valueOf(dto.getWelcome().isAiEnabled()));
        saveSetting(guildId, guildName, map, "welcome.ai.prompt", dto.getWelcome().getAiPrompt());
        saveSetting(guildId, guildName, map, "welcome.role.onjoin.enabled", String.valueOf(dto.getWelcome().isRoleOnJoinEnabled()));
        saveSetting(guildId, guildName, map, "welcome.role.onjoin.roleIds", joinIds(dto.getWelcome().getRoleOnJoinIds()));
    }

    private void saveQuitSettings(String guildId, String guildName, Map<String, GuildSetting> map, GuildSettingsDto dto) {
        saveSetting(guildId, guildName, map, "quit.enabled", String.valueOf(dto.getQuit().isEnabled()));
        saveSetting(guildId, guildName, map, "quit.message", dto.getQuit().getMessage());
        saveSetting(guildId, guildName, map, "quit.channelIds", joinIds(dto.getQuit().getChannelIds()));
    }

    private void saveMcWhitelistSettings(String guildId, String guildName, Map<String, GuildSetting> map, GuildSettingsDto dto) {
        saveSetting(guildId, guildName, map, "mc-whitelist.enabled", String.valueOf(dto.getMcWhitelist().isEnabled()));
    }

    private List<String> splitIds(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String joinIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return String.join(",", ids);
    }
}
