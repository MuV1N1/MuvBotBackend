package de.muv1n.muvbot.repository;

import de.muv1n.muvbot.entity.extensions.mc.McServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface McServerRepository extends JpaRepository<McServer, String> {
    List<McServer> findByGuildId(String guildId);
}
