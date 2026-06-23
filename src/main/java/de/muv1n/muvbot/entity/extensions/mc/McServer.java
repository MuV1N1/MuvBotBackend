package de.muv1n.muvbot.entity.extensions.mc;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mc_servers")
@Data
public class McServer {

    @Id
    private String id;

    @Column(name = "guild_id", nullable = false)
    private String guildId;

    @Column(name = "server_name", nullable = false)
    private String serverName;

    @Column(name = "server_ip")
    private String serverIp;

    @Column(name = "server_version")
    private String serverVersion;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mc_server_whitelist_roles", joinColumns = @JoinColumn(name = "server_id"))
    @Column(name = "role_id")
    private List<String> whitelistRoleIds = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
