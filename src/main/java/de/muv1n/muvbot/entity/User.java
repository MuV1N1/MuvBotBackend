package de.muv1n.muvbot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discord_id", unique = true, nullable = false)
    private String discordId;

    private String username;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "minecraft_username")
    private String minecraftUsername;

    @Column(name = "minecraft_uuid")
    private String minecraftUuid;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_hidden_guilds", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "guild_id")
    private Set<String> hiddenGuildIds = new HashSet<>();
}
