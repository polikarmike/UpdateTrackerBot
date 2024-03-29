package edu.java.scrapper.domain.repository.jpa;

import edu.java.scrapper.dto.entity.Link;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JPALinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUri(URI uri);

    @Query(value = "SELECT COUNT(*) > 0 FROM Link_Chat WHERE chat_id = ? AND link_id = ?", nativeQuery = true)
    boolean exists(@Param("chat_id") long chatId, @Param("link_id") long linkId);

    @Query(value = "SELECT * FROM Link ORDER BY last_updated_at ASC LIMIT ?", nativeQuery = true)
    List<Link> findOldestLinks(@Param("limit") int limit);

    @Query("SELECT l FROM Link l LEFT JOIN l.chats c WHERE c IS NULL")
    List<Link> findUnusedLinks();
}
