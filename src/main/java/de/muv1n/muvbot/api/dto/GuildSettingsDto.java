package de.muv1n.muvbot.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GuildSettingsDto {
    private GeneralSettings general = new GeneralSettings();
    private WelcomeSettings welcome = new WelcomeSettings();
    private QuitSettings quit = new QuitSettings();
    private McWhitelistSettings mcWhitelist = new McWhitelistSettings();
    private TicketSystemSettings ticketSystem = new TicketSystemSettings();

    @Data
    public static class GeneralSettings {
        private String name;
        private String commandPrefix;
    }

    @Data
    public static class WelcomeSettings {
        private boolean enabled;
        private String message;
        private List<String> channelIds = new ArrayList<>();
        private boolean aiEnabled;
        private String aiPrompt;
        private boolean roleOnJoinEnabled;
        private List<String> roleOnJoinIds = new ArrayList<>();
    }

    @Data
    public static class QuitSettings {
        private boolean enabled;
        private String message;
        private List<String> channelIds = new ArrayList<>();
    }


    @Data
    public static class McWhitelistSettings {
        private boolean enabled;
    }

    @Data
    public static class TicketSystemSettings {
        private boolean enabled;
    }
}

