package de.muv1n.muvbot.service.extensions.mc;

import de.muv1n.muvbot.entity.extensions.mc.McServer;
import de.muv1n.muvbot.entity.User;
import de.muv1n.muvbot.repository.McServerRepository;
import de.muv1n.muvbot.repository.UserRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class McWhitelistService {

    private final McServerRepository mcServerRepository;
    private final UserRepository userRepository;
    private final JDA jda;

    public McWhitelistService(McServerRepository mcServerRepository, UserRepository userRepository, JDA jda) {
        this.mcServerRepository = mcServerRepository;
        this.userRepository = userRepository;
        this.jda = jda;
    }

    public List<String> getWhitelist(String apiKey) {
        McServer server = mcServerRepository.findById(apiKey).orElse(null);
        if (server == null) return null;

        Guild guild = jda.getGuildById(server.getGuildId());
        if (guild == null || server.getWhitelistRoleIds().isEmpty()) return List.of();

        Set<String> eligibleDiscordIds = new HashSet<>();
        for (String roleId : server.getWhitelistRoleIds()) {
            Role role = guild.getRoleById(roleId);
            if (role == null) continue;
            guild.getMembersWithRoles(role).stream()
                    .map(Member::getId)
                    .forEach(eligibleDiscordIds::add);
        }

        return eligibleDiscordIds.stream()
                .map(id -> userRepository.findByDiscordId(id).orElse(null))
                .filter(u -> u != null && u.getMinecraftUsername() != null)
                .map(User::getMinecraftUsername)
                .collect(Collectors.toList());
    }

    public List<McServer> getServers(String guildId) {
        return mcServerRepository.findByGuildId(guildId);
    }

    public McServer createServer(String guildId, String serverName, String serverIp) {
        McServer server = new McServer();
        server.setId(UUID.randomUUID().toString());
        server.setGuildId(guildId);
        server.setServerName(serverName);
        server.setServerIp(serverIp);
        server.setCreatedAt(LocalDateTime.now());
        return mcServerRepository.save(server);
    }

    public boolean deleteServer(String guildId, String serverId) {
        McServer server = mcServerRepository.findById(serverId).orElse(null);
        if (server == null || !server.getGuildId().equals(guildId)) return false;
        mcServerRepository.delete(server);
        return true;
    }

    public McServer updateRoles(String guildId, String serverId, List<String> roleIds) {
        McServer server = mcServerRepository.findById(serverId).orElse(null);
        if (server == null || !server.getGuildId().equals(guildId)) return null;
        server.setWhitelistRoleIds(roleIds != null ? roleIds : new ArrayList<>());
        return mcServerRepository.save(server);
    }
}
