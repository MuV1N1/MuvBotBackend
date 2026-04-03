package de.muv1n.muvbot.bot;

import de.muv1n.muvbot.ai.SpringAi;
import de.muv1n.muvbot.repository.GuildSettingRepository;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class QuitListener extends ListenerAdapter {

    private final GuildSettingRepository settingRepository;



    public QuitListener(GuildSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    private Logger logger = org.slf4j.LoggerFactory.getLogger(QuitListener.class);

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {

        logger.info("User " + event.getUser().getAsTag() + " left the server");
        String guildId = event.getGuild().getId();

        boolean enabled = settingRepository.findByGuildIdAndKey(guildId, "quit.enabled")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);
        if (!enabled) return;
        QuitMessage(event, guildId);
    }

    private void QuitMessage(GuildMemberRemoveEvent event, String guildId) {

        String channelId = settingRepository.findByGuildIdAndKey(guildId, "quit.channelId").map(s -> s.getValue()).orElse("");

        TextChannel channel = event.getGuild().getTextChannelById(channelId);

        String messageTemplate = settingRepository.findByGuildIdAndKey(guildId, "quit.message")
                    .map(s -> s.getValue())
                    .orElse("{user} left the server!");

        String message = messageTemplate.replace("{user}", event.getUser().getAsMention());

        channel.sendMessage(message).queue();
    }
}
