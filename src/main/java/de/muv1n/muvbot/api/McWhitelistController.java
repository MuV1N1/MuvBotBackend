package de.muv1n.muvbot.api;

import de.muv1n.muvbot.api.dto.extensions.mc.McServerCreateDto;
import de.muv1n.muvbot.api.dto.extensions.mc.McServerOrderDto;
import de.muv1n.muvbot.api.dto.extensions.mc.McServerRolesDto;
import de.muv1n.muvbot.api.dto.extensions.mc.McServerUpdateDto;
import de.muv1n.muvbot.api.dto.extensions.mc.WhitelistDto;
import de.muv1n.muvbot.entity.Guild;
import de.muv1n.muvbot.entity.extensions.mc.McServer;
import de.muv1n.muvbot.service.GuildService;
import de.muv1n.muvbot.service.extensions.mc.McWhitelistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class McWhitelistController {

    private final McWhitelistService mcWhitelistService;
    private final GuildService guildService;

    public McWhitelistController(McWhitelistService mcWhitelistService, GuildService guildService) {
        this.mcWhitelistService = mcWhitelistService;
        this.guildService = guildService;
    }

    /** Called by the Minecraft plugin — authenticated by API key only. */
    @GetMapping("/api/mc/whitelist")
    public ResponseEntity<List<String>> getWhitelist(@RequestHeader("X-Api-Key") String apiKey) {
        List<String> whitelist = mcWhitelistService.getWhitelist(apiKey);
        if (whitelist == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(whitelist);
    }

    @GetMapping("/api/guilds/{guildId}/mc/servers")
    public ResponseEntity<List<WhitelistDto>> getServers(
            @PathVariable String guildId,
            @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();

        List<WhitelistDto> servers = mcWhitelistService.getServers(guildId).stream()
                .map(s -> new WhitelistDto(s.getId(), s.getServerName(), s.getServerIp(), s.getServerVersion(), s.getWhitelistRoleIds()))
                .toList();
        return ResponseEntity.ok(servers);
    }

    @PostMapping("/api/guilds/{guildId}/mc/servers")
    public ResponseEntity<WhitelistDto> createServer(
            @PathVariable String guildId,
            @RequestBody McServerCreateDto dto,
            @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();

        McServer server = mcWhitelistService.createServer(guildId, dto.getServerName(), dto.getServerIp(), dto.getServerVersion());
        return ResponseEntity.ok(new WhitelistDto(server.getId(), server.getServerName(), server.getServerIp(), server.getServerVersion(), server.getWhitelistRoleIds()));
    }

    @DeleteMapping("/api/guilds/{guildId}/mc/servers/{serverId}")
    public ResponseEntity<Void> deleteServer(
            @PathVariable String guildId,
            @PathVariable String serverId,
            @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();

        boolean deleted = mcWhitelistService.deleteServer(guildId, serverId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PatchMapping("/api/guilds/{guildId}/mc/servers/{serverId}")
    public ResponseEntity<WhitelistDto> updateServer(
            @PathVariable String guildId,
            @PathVariable String serverId,
            @RequestBody McServerUpdateDto dto,
            @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();
        McServer server = mcWhitelistService.updateServer(guildId, serverId, dto.getServerName(), dto.getServerIp(), dto.getServerVersion());
        if (server == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new WhitelistDto(server.getId(), server.getServerName(), server.getServerIp(), server.getServerVersion(), server.getWhitelistRoleIds()));
    }

    @PutMapping("/api/guilds/{guildId}/mc/servers/order")
    public ResponseEntity<Void> updateOrder(
            @PathVariable String guildId,
            @RequestBody McServerOrderDto dto,
            @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();
        mcWhitelistService.updateOrder(guildId, dto.getServerIds());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/guilds/{guildId}/mc/servers/{serverId}/roles")
    public ResponseEntity<WhitelistDto> updateRoles(
            @PathVariable String guildId,
            @PathVariable String serverId,
            @RequestBody McServerRolesDto dto,
            @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();

        McServer server = mcWhitelistService.updateRoles(guildId, serverId, dto.getRoleIds());
        if (server == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new WhitelistDto(server.getId(), server.getServerName(), server.getServerIp(), server.getServerVersion(), server.getWhitelistRoleIds()));
    }
}
