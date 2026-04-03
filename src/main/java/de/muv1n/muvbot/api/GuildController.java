package de.muv1n.muvbot.api;

import de.muv1n.muvbot.api.dto.ChannelDto;
import de.muv1n.muvbot.api.dto.GuildSettingsDto;
import de.muv1n.muvbot.api.dto.RolesDto;
import de.muv1n.muvbot.entity.Guild;
import de.muv1n.muvbot.service.GuildService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guilds")
public class GuildController {

    private final GuildService guildService;

    public GuildController(GuildService guildService) {
        this.guildService = guildService;
    }

    @GetMapping
    public ResponseEntity<List<Guild>> getKey(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(guildService.getMutualGuilds(token));
    }

    @GetMapping("/{guildId}")
    public ResponseEntity<Guild> getGuild(@PathVariable String guildId, @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(guild);
    }

    @GetMapping("/{guildId}/settings")
    public ResponseEntity<GuildSettingsDto> getSettings(@PathVariable String guildId, @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(guildService.getSettings(guildId));
    }

    @PutMapping("/{guildId}/settings")
    public ResponseEntity<Void> updateSettings(@PathVariable String guildId, @RequestBody GuildSettingsDto settings, @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();
        guildService.updateSettings(guildId, settings);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{guildId}/channels")
    public ResponseEntity<List<ChannelDto>> getChannels(@PathVariable String guildId, @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(guildService.getChannels(guildId));
    }

    @GetMapping("/{guildId}/roles")
    public ResponseEntity<List<RolesDto>> getRoles(@PathVariable String guildId, @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(guildService.getRoles(guildId));
    }

}
