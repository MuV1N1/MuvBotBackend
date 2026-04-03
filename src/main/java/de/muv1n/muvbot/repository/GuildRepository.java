package de.muv1n.muvbot.repository;

import de.muv1n.muvbot.entity.Guild;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuildRepository extends JpaRepository<Guild, String> {
}
