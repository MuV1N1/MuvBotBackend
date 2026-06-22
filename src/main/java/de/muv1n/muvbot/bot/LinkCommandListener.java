package de.muv1n.muvbot.bot;

import de.muv1n.muvbot.entity.User;
import de.muv1n.muvbot.repository.UserRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class LinkCommandListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(LinkCommandListener.class);
    private static final String MOJANG_API = "https://api.mojang.com/users/profiles/minecraft/";

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public LinkCommandListener(UserRepository userRepository, RestTemplate discordRestTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = discordRestTemplate;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("link")) return;

        event.deferReply(true).queue(); // ephemeral — only the user sees the response

        String discordId = event.getUser().getId();
        String mcUsername = event.getOption("username").getAsString().trim();

        try {
            // Validate username + fetch UUID from Mojang
            @SuppressWarnings("unchecked")
            Map<String, String> mojang = restTemplate.getForObject(MOJANG_API + mcUsername, Map.class);

            if (mojang == null || !mojang.containsKey("id")) {
                event.getHook().sendMessage("❌ **" + mcUsername + "** was not found on Mojang. Check the spelling and try again.").queue();
                return;
            }

            String rawUuid = mojang.get("id"); // no dashes
            String confirmedName = mojang.get("name"); // exact capitalisation from Mojang
            String formattedUuid = formatUuid(rawUuid);

            User user = userRepository.findByDiscordId(discordId).orElseGet(() -> {
                User u = new User();
                u.setDiscordId(discordId);
                u.setUsername(event.getUser().getName());
                return u;
            });

            user.setMinecraftUsername(confirmedName);
            user.setMinecraftUuid(formattedUuid);
            userRepository.save(user);

            logger.info("Linked Discord {} to Minecraft {} ({})", discordId, confirmedName, formattedUuid);
            event.getHook().sendMessage("✅ Linked your Discord account to **" + confirmedName + "**. You will be added to any configured whitelists within the next sync.").queue();

        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            event.getHook().sendMessage("❌ **" + mcUsername + "** was not found on Mojang. Check the spelling and try again.").queue();
        } catch (Exception e) {
            logger.error("Failed to link Minecraft account for Discord user {}", discordId, e);
            event.getHook().sendMessage("❌ Something went wrong. Please try again later.").queue();
        }
    }

    public static void registerCommand(Guild guild) {
        guild.updateCommands().addCommands(
                Commands.slash("link", "Link your Discord account to your Minecraft username")
                        .addOption(OptionType.STRING, "username", "Your Minecraft username", true)
        ).queue();
    }

    public static void unregisterCommand(Guild guild) {
        guild.updateCommands().queue(); // clears all guild-specific commands
    }

    private String formatUuid(String raw) {
        // "069a79f444e94726a5befca90e38aaf5" → "069a79f4-44e9-4726-a5be-fca90e38aaf5"
        return UUID.fromString(
            raw.substring(0, 8) + "-" +
            raw.substring(8, 12) + "-" +
            raw.substring(12, 16) + "-" +
            raw.substring(16, 20) + "-" +
            raw.substring(20)
        ).toString();
    }
}
