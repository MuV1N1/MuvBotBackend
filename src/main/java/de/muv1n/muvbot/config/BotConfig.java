package de.muv1n.muvbot.config;

import de.muv1n.muvbot.bot.BotListener;
import de.muv1n.muvbot.bot.JoinListener;
import de.muv1n.muvbot.bot.LinkCommandListener;
import de.muv1n.muvbot.bot.QuitListener;
import de.muv1n.muvbot.repository.GuildSettingRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${discord.bot.token}")
    private String botToken;

    private final BotListener botListener;
    private final JoinListener joinListener;
    private final QuitListener quitListener;
    private final LinkCommandListener linkCommandListener;
    private final GuildSettingRepository guildSettingRepository;

    public BotConfig(BotListener botListener, JoinListener joinListener, QuitListener quitListener,
                     LinkCommandListener linkCommandListener, GuildSettingRepository guildSettingRepository) {
        this.botListener = botListener;
        this.joinListener = joinListener;
        this.quitListener = quitListener;
        this.linkCommandListener = linkCommandListener;
        this.guildSettingRepository = guildSettingRepository;
    }

    @Bean
    public JDA jda() throws InterruptedException {
        JDA jda = JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(botListener, joinListener, quitListener, linkCommandListener)
                .build()
                .awaitReady();

        // Register /link only in guilds that already have mc-whitelist enabled
        jda.getGuilds().forEach(guild ->
                guildSettingRepository.findByGuildIdAndKey(guild.getId(), "mc-whitelist.enabled")
                        .filter(s -> "true".equalsIgnoreCase(s.getValue()))
                        .ifPresent(s -> LinkCommandListener.registerCommand(guild))
        );

        return jda;
    }
}
