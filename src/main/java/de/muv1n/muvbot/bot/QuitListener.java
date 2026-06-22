package de.muv1n.muvbot.bot;

import de.muv1n.muvbot.repository.GuildSettingRepository;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class QuitListener extends ListenerAdapter {

    private final GuildSettingRepository settingRepository;
    private static final Logger logger = LoggerFactory.getLogger(QuitListener.class);

    public QuitListener(GuildSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        logger.info("User {} left the server", event.getUser().getAsTag());
        String guildId = event.getGuild().getId();

        boolean enabled = settingRepository.findByGuildIdAndKey(guildId, "quit.enabled")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);
        if (!enabled) return;

        sendQuitMessages(event, guildId);
    }

    private void sendQuitMessages(GuildMemberRemoveEvent event, String guildId) {
        List<String> channelIds = splitIds(
                settingRepository.findByGuildIdAndKey(guildId, "quit.channelIds")
                        .map(s -> s.getValue()).orElse(""));

        if (channelIds.isEmpty()) return;

        String messageTemplate = settingRepository.findByGuildIdAndKey(guildId, "quit.message")
                .map(s -> s.getValue())
                .orElse("{user} left the server!");

        String message = messageTemplate.replace("{user}", event.getUser().getAsMention());

        for (String channelId : channelIds) {
            TextChannel channel = event.getGuild().getTextChannelById(channelId);
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }
    }

    private List<String> splitIds(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
