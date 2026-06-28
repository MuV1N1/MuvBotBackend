package de.muv1n.muvbot.api.controller;

import de.muv1n.muvbot.entity.User;
import de.muv1n.muvbot.repository.UserRepository;
import de.muv1n.muvbot.service.GuildService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final GuildService guildService;

    public UserController(UserRepository userRepository, GuildService guildService) {
        this.userRepository = userRepository;
        this.guildService = guildService;
    }

    @PostMapping("/hide/{guildId}")
    public ResponseEntity<Void> hideGuild(@PathVariable String guildId, @RequestHeader("Authorization") String token) {
        String userId = guildService.getUserId(token);
        if (userId == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByDiscordId(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.getHiddenGuildIds().add(guildId);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/hide/{guildId}")
    public ResponseEntity<Void> unhideGuild(@PathVariable String guildId, @RequestHeader("Authorization") String token) {
        String userId = guildService.getUserId(token);
        if (userId == null) return ResponseEntity.status(401).build();

        User user = userRepository.findByDiscordId(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        if (user.getHiddenGuildIds().contains(guildId)) {
            user.getHiddenGuildIds().remove(guildId);
            userRepository.save(user);
        }

        return ResponseEntity.ok().build();
    }
}
