package de.muv1n.muvbot.api.controller;

import de.muv1n.muvbot.entity.Guild;
import de.muv1n.muvbot.service.GuildService;
import de.muv1n.muvbot.service.TicketSystemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guilds")
public class TicketSystemController {

    private final TicketSystemService ticketSystemService;
    private final GuildService guildService;

    public TicketSystemController(TicketSystemService ticketSystemService, GuildService guildService) {
        this.ticketSystemService = ticketSystemService;
        this.guildService = guildService;
    }

    /** Posts the configured ticket panel (embed + open button) into the panel channel. */
    @PostMapping("/{guildId}/ticket-system/panel")
    public ResponseEntity<Void> sendPanel(@PathVariable String guildId, @RequestHeader("Authorization") String token) {
        Guild guild = guildService.getGuild(guildId, token);
        if (guild == null) return ResponseEntity.notFound().build();

        boolean sent = ticketSystemService.sendPanel(guildId);
        return sent ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
