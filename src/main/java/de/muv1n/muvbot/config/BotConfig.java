package de.muv1n.muvbot.config;

import de.muv1n.muvbot.bot.BotListener;
import de.muv1n.muvbot.bot.JoinListener;
import de.muv1n.muvbot.bot.QuitListener;
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


    public BotConfig(BotListener botListener, JoinListener joinListener, QuitListener quitListener) {
        this.botListener = botListener;
        this.joinListener = joinListener;
        this.quitListener = quitListener;
    }

    @Bean
    public JDA jda() throws InterruptedException {
        return JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(botListener, joinListener, quitListener)
                .build()
                .awaitReady();
    }
}
