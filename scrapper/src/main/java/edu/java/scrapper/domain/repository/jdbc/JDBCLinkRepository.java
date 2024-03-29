package edu.java.scrapper.domain.repository.jdbc;

import edu.java.scrapper.domain.repository.LinkRepository;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.exception.DataBaseError;
import edu.java.scrapper.utils.mappers.LinkMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class JDBCLinkRepository  implements LinkRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final String LINK_CREATION_ERROR_MESSAGE = "Link was not created";
    private static final String INVALID_LINK_ERROR_MESSAGE = "Invalid Link";

    @Override
    public Link add(URI uri) {
        OffsetDateTime now = OffsetDateTime.now();
        jdbcTemplate.update("INSERT INTO Link (url, created_at, last_updated_at) VALUES (?, ?, ?)",
            uri.toString(), now, now);
        return getLinkByUri(uri).orElseThrow(() -> new DataBaseError(LINK_CREATION_ERROR_MESSAGE));
    }

    @Override
    public void remove(URI uri) {
        jdbcTemplate.update("DELETE FROM Link WHERE url = ?", uri.toString());
    }

    @Override
    public void remove(long id) {
        jdbcTemplate.update("DELETE FROM Link WHERE id = ?", id);
    }

    @Override
    public Optional<Link> getLinkByUri(URI uri) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT * FROM Link WHERE url = ?",
                (rs, rowNum) -> Optional.of(LinkMapper.mapRowToLink(rs)),
                uri.toString());
        } catch (Exception e) {
            return Optional.empty();
        }

    }

    @Override
    public Optional<Link> getLinkById(long id) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT * FROM Link WHERE id = ?",
                (rs, rowNum) -> Optional.of(LinkMapper.mapRowToLink(rs)),
                id
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateLastUpdatedTime(long id) {
        jdbcTemplate.update(
            "UPDATE Link SET last_updated_at = ? WHERE id = ?",
            OffsetDateTime.now(), id);
    }

    @Override
    public List<Link> findAll() {
        return jdbcTemplate.query(
            "SELECT * FROM Link",
            (rs, rowNum) -> LinkMapper.mapRowToLink(rs));
    }

    @Override
    public List<Link> findOldestLinks(int batchSize) {
        return jdbcTemplate.query(
            "SELECT * FROM Link ORDER BY last_updated_at ASC LIMIT ?",
            (rs, rowNum) -> LinkMapper.mapRowToLink(rs), batchSize
        );
    }
}
