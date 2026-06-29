package de.muv1n.muvbot.api.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TicketSystemDto {
    private boolean enabled;
    private Panel panel = new Panel();
    private Ticket ticket = new Ticket();
    private Access access = new Access();

    @Data
    public static class Panel {
        private String channelId;
        private String title;
        private String description;
        private String buttonLabel;
        private String buttonEmoji;
    }

    @Data
    public static class Ticket {
        private String categoryId;
        private String logChannelId;
        private String namePrefix;
        private int maxOpenTicketsPerUser;
    }

    @Data
    public static class Access {
        private List<String> supportRoleIds = new ArrayList<>();
        private List<String> adminRoleIds = new ArrayList<>();
    }
}
