package edu.java.scrapper.domain.repository.jpa;

import edu.java.scrapper.dto.entity.Link;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JPALinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUri(URI uri);

    boolean existsByChatsIdAndId(long chatId, long linkId);

    List<Link> findAllByOrderByLastUpdatedAtAsc(Pageable pageable);

    List<Link> findByChatsIsEmpty();
}
