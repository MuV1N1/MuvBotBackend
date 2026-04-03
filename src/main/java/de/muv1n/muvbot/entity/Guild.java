package de.muv1n.muvbot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "guilds")
@Data
public class Guild {
    @Id
    @Column(name = "discord_id")
    private String discordId;

    @Column(name = "name")
    private String name;

    @Column(name = "owner_id")
    private String ownerId;
    
    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "bot_installed")
    private boolean botInstalled;

    @Column(name = "welcome_active")
    private boolean welcomeActive;

    @Transient
    private boolean isHidden;
}
