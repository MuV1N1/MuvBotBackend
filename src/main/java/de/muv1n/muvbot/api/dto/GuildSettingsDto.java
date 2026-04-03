package de.muv1n.muvbot.api.dto;
import lombok.Data;

@Data
public class GuildSettingsDto {
    private GeneralSettings general = new GeneralSettings();
    private WelcomeSettings welcome = new WelcomeSettings();
    private ModerationSettings moderation = new ModerationSettings();
    private QuitSettings quit = new QuitSettings();

    @Data
    public static class GeneralSettings {
        private String name;
        private String commandPrefix;
    }

    @Data
    public static class WelcomeSettings {
        private boolean enabled;
        private String message;
        private String channelId;
        private String channelName;
        private boolean aiEnabled;
        private String aiPrompt;
        private boolean roleOnJoinEnabled;
        private String roleOnJoinId;
        private String roleOnJoinName;

    }

    @Data
    public static class QuitSettings {
        private boolean enabled;
        private String message;
        private String channelId;
        private String channelName;
    }

    @Data
    public static class ModerationSettings {
        private boolean enabled;
    }
}
