package de.muv1n.muvbot.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "guild_settings")
@Data
public class GuildSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "setting_key", nullable = false)
    private String key;

    @Column(name = "setting_value")
    private String value;

}
