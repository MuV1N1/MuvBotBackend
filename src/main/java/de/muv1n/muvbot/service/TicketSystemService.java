package de.muv1n.muvbot.service;

import de.muv1n.muvbot.api.dto.TicketSystemDto;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TicketSystemService {

    /** Custom id of the "open ticket" button — consumed by the bot's button handler. */
    public static final String OPEN_BUTTON_ID = "ticket:open";

    private static final Logger logger = LoggerFactory.getLogger(TicketSystemService.class);

    private final JDA jda;
    private final GuildService guildService;

    public TicketSystemService(JDA jda, GuildService guildService) {
        this.jda = jda;
        this.guildService = guildService;
    }

    /**
     * Posts the ticket panel (embed + open button) into the configured panel channel.
     *
     * @return {@code true} if the panel was sent, {@code false} if it is not configured
     *         or the channel could not be resolved.
     */
    public boolean sendPanel(String guildId) {
        TicketSystemDto.Panel panel = guildService.getSettings(guildId).getTicketSystem().getPanel();

        if (panel.getChannelId() == null || panel.getChannelId().isBlank()) {
            logger.warn("Cannot send ticket panel for guild {}: no panel channel configured", guildId);
            return false;
        }

        net.dv8tion.jda.api.entities.Guild jdaGuild = jda.getGuildById(guildId);
        if (jdaGuild == null) {
            logger.warn("Cannot send ticket panel for guild {}: bot is not in the guild", guildId);
            return false;
        }

        TextChannel channel = jdaGuild.getTextChannelById(panel.getChannelId());
        if (channel == null) {
            logger.warn("Cannot send ticket panel for guild {}: channel {} not found", guildId, panel.getChannelId());
            return false;
        }

        String title = panel.getTitle() == null || panel.getTitle().isBlank() ? "Support Tickets" : panel.getTitle();
        String description = panel.getDescription() == null || panel.getDescription().isBlank()
                ? "Open a ticket if you need help."
                : panel.getDescription();
        String buttonLabel = panel.getButtonLabel() == null || panel.getButtonLabel().isBlank()
                ? "Create Ticket"
                : panel.getButtonLabel();

        Button button = Button.primary(OPEN_BUTTON_ID, buttonLabel);
        if (panel.getButtonEmoji() != null && !panel.getButtonEmoji().isBlank()) {
            try {
                button = button.withEmoji(Emoji.fromFormatted(panel.getButtonEmoji()));
            } catch (IllegalArgumentException e) {
                logger.warn("Ignoring invalid ticket button emoji '{}' for guild {}", panel.getButtonEmoji(), guildId);
            }
        }

        channel.sendMessageEmbeds(new EmbedBuilder().setTitle(title).setDescription(description).build())
                .setComponents(ActionRow.of(button))
                .queue();

        return true;
    }
}
