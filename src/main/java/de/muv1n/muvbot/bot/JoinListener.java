package de.muv1n.muvbot.bot;

import de.muv1n.muvbot.ai.SpringAi;
import de.muv1n.muvbot.repository.GuildSettingRepository;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JoinListener extends ListenerAdapter {

    private final GuildSettingRepository settingRepository;

    @Autowired(required = false)
    private SpringAi ai;

    public JoinListener(GuildSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    private Logger logger = org.slf4j.LoggerFactory.getLogger(JoinListener.class);

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String guildId = event.getGuild().getId();

        boolean enabled = settingRepository.findByGuildIdAndKey(guildId, "welcome.enabled")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);
        if (!enabled) return;

        JoinMessage(event, guildId);
        addRoleToUser(event, guildId);
    }

    private void JoinMessage(GuildMemberJoinEvent event, String guildId) {

        boolean aiEnabled = settingRepository.findByGuildIdAndKey(guildId, "welcome.ai.message")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);

        String channelId = settingRepository.findByGuildIdAndKey(guildId, "welcome.channelId").map(s -> s.getValue()).orElse("");

        TextChannel channel = event.getGuild().getTextChannelById(channelId);

        String messageTemplate = "";

        String promptDefault = " AB HIER SPRACHE IGNORIEREN IMMER IN DER SPRACHE DES BEGINNS SCHREIBEN"
                + ", wenn Sprache gleich Deutsch dann schreibe deine Nachricht"
                + "Auch deutsch wenn englisch dann schreibe auch englisch usw"
                + "Schreibe eine kurze, knackige Begrüßungsnachricht für einen Discord-Server. "
                + "Wichtig: Gib ausschließlich die fertige Begrüßungsnachricht aus, "
                + "Keine Meta-Texte wie 'Hier ist die Nachricht' o. Ä. ,"
                + "Die Nachricht soll direkt im Discord verwendbar sein.,"
                + "Verwende den Platzhalter '{user}' für den Namen des neuen Mitglieds., "
                + "Es handel sich um ein Begrüßungsnachrichten-Bot.";

        String prompt = settingRepository.findByGuildIdAndKey(guildId, "welcome.ai.prompt").map(s -> s.getValue()).orElse(promptDefault);

        String finalPrompt = prompt + " " + promptDefault;

        messageTemplate = ai.prompt(finalPrompt);
        assert channel != null;
        if (!aiEnabled) {
            messageTemplate = settingRepository.findByGuildIdAndKey(guildId, "welcome.message")
                    .map(s -> s.getValue())
                    .orElse("Welcome {user} to the server!");
        }


        String message = messageTemplate.replace("{user}", event.getMember().getAsMention());

        channel.sendMessage(message).queue();
    }

    private void addRoleToUser(GuildMemberJoinEvent event, String guildId) {

        boolean roleEnabled = settingRepository.findByGuildIdAndKey(guildId, "welcome.role.onjoin.enabled")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);
        logger.info("Role on join enabled: " + roleEnabled);
        if (!roleEnabled) return;

        String roleId = settingRepository.findByGuildIdAndKey(guildId, "welcome.role.onjoin.roleId")
                .map(s -> s.getValue())
                .orElse("");

        if (roleId.isEmpty()) return;
        event.getGuild().addRoleToMember(event.getMember(), Objects.requireNonNull(event.getGuild().getRoleById(roleId))).queue();
        logger.info("Role added to user: " + event.getMember().getAsMention());
    }
}
