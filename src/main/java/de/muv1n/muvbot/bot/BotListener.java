package de.muv1n.muvbot.bot;

import de.muv1n.muvbot.ai.SpringAi;
import de.muv1n.muvbot.entity.Guild;
import de.muv1n.muvbot.entity.GuildSetting;
import de.muv1n.muvbot.repository.GuildRepository;
import de.muv1n.muvbot.repository.GuildSettingRepository;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BotListener extends ListenerAdapter {

    private final GuildRepository guildRepository;
    private final GuildSettingRepository guildSettingRepository;
    private static final Logger logger = LoggerFactory.getLogger(BotListener.class);

    @Autowired(required = false)
    private SpringAi ai;

    public BotListener(GuildRepository guildRepository, GuildSettingRepository guildSettingRepository) {
        this.guildRepository = guildRepository;
        this.guildSettingRepository = guildSettingRepository;
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = new Guild();
        guild.setDiscordId(event.getGuild().getId());
        guild.setName(event.getGuild().getName());
        guild.setOwnerId(event.getGuild().getOwnerId());
        guild.setBotInstalled(true);

        String icon = event.getGuild().getIconId();
        if (icon != null) {
            guild.setIconUrl("https://cdn.discordapp.com/icons/" + event.getGuild().getId() + "/" + icon + ".png");
        }
        guildRepository.save(guild);
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        String guildId = event.getGuild().getId();
        
        List<GuildSetting> settings = guildSettingRepository.findByGuildId(guildId);
        if (!settings.isEmpty()) {
            guildSettingRepository.deleteAll(settings);
        }
        
        guildRepository.deleteById(guildId);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMessage().getContentRaw().equals("!ping")) {
            event.getChannel().sendMessage("Pong!").queue();
        }
    }
}
