package de.muv1n.muvbot.repository;

import de.muv1n.muvbot.entity.extensions.mc.McServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface McServerRepository extends JpaRepository<McServer, String> {
    @org.springframework.data.jpa.repository.Query("SELECT s FROM McServer s WHERE s.guildId = :guildId ORDER BY COALESCE(s.sortOrder, 999999) ASC")
    List<McServer> findByGuildIdOrderBySortOrder(@org.springframework.data.repository.query.Param("guildId") String guildId);
}
