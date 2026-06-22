package de.muv1n.muvbot.bot;

import de.muv1n.muvbot.ai.SpringAi;
import de.muv1n.muvbot.repository.GuildSettingRepository;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class JoinListener extends ListenerAdapter {

    private final GuildSettingRepository settingRepository;
    private static final Logger logger = LoggerFactory.getLogger(JoinListener.class);

    @Autowired(required = false)
    private SpringAi ai;

    public JoinListener(GuildSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        String guildId = event.getGuild().getId();

        boolean enabled = settingRepository.findByGuildIdAndKey(guildId, "welcome.enabled")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);
        if (!enabled) return;

        sendJoinMessages(event, guildId);
        addRolesToUser(event, guildId);
    }

    private void sendJoinMessages(GuildMemberJoinEvent event, String guildId) {
        List<String> channelIds = splitIds(
                settingRepository.findByGuildIdAndKey(guildId, "welcome.channelIds")
                        .map(s -> s.getValue()).orElse(""));

        if (channelIds.isEmpty()) return;

        boolean aiEnabled = settingRepository.findByGuildIdAndKey(guildId, "welcome.ai.message")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);

        String messageTemplate;
        if (aiEnabled && ai != null) {
            String promptDefault = " AB HIER SPRACHE IGNORIEREN IMMER IN DER SPRACHE DES BEGINNS SCHREIBEN"
                    + ", wenn Sprache gleich Deutsch dann schreibe deine Nachricht"
                    + "Auch deutsch wenn englisch dann schreibe auch englisch usw"
                    + "Schreibe eine kurze, knackige Begrüßungsnachricht für einen Discord-Server. "
                    + "Wichtig: Gib ausschließlich die fertige Begrüßungsnachricht aus, "
                    + "Keine Meta-Texte wie 'Hier ist die Nachricht' o. Ä. ,"
                    + "Die Nachricht soll direkt im Discord verwendbar sein.,"
                    + "Verwende den Platzhalter '{user}' für den Namen des neuen Mitglieds., "
                    + "Es handel sich um ein Begrüßungsnachrichten-Bot.";
            String prompt = settingRepository.findByGuildIdAndKey(guildId, "welcome.ai.prompt")
                    .map(s -> s.getValue()).orElse(promptDefault);
            messageTemplate = ai.prompt(prompt + " " + promptDefault);
        } else {
            messageTemplate = settingRepository.findByGuildIdAndKey(guildId, "welcome.message")
                    .map(s -> s.getValue())
                    .orElse("Welcome {user} to the server!");
        }

        String message = messageTemplate.replace("{user}", event.getMember().getAsMention());

        for (String channelId : channelIds) {
            TextChannel channel = event.getGuild().getTextChannelById(channelId);
            if (channel != null) {
                channel.sendMessage(message).queue();
            }
        }
    }

    private void addRolesToUser(GuildMemberJoinEvent event, String guildId) {
        boolean roleEnabled = settingRepository.findByGuildIdAndKey(guildId, "welcome.role.onjoin.enabled")
                .map(s -> s.getValue().equalsIgnoreCase("true"))
                .orElse(false);
        if (!roleEnabled) return;

        List<String> roleIds = splitIds(
                settingRepository.findByGuildIdAndKey(guildId, "welcome.role.onjoin.roleIds")
                        .map(s -> s.getValue()).orElse(""));

        for (String roleId : roleIds) {
            Role role = event.getGuild().getRoleById(roleId);
            if (role == null) continue;
            try {
                event.getGuild().addRoleToMember(event.getMember(), role).queue(
                        success -> logger.info("Role {} added to {}", roleId, event.getMember().getEffectiveName()),
                        error  -> logger.warn("Failed to add role {} to {}: {}", roleId, event.getMember().getEffectiveName(), error.getMessage())
                );
            } catch (net.dv8tion.jda.api.exceptions.HierarchyException e) {
                logger.warn("Cannot assign role {} — it is higher than the bot's own highest role", roleId);
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
