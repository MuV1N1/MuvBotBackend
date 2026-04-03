package de.muv1n.muvbot.repository;

import de.muv1n.muvbot.entity.GuildSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuildSettingRepository extends JpaRepository<GuildSetting, Long> {
    List<GuildSetting> findByGuildId(String guildId);
    Optional<GuildSetting> findByGuildIdAndKey(String guildId, String key);
}