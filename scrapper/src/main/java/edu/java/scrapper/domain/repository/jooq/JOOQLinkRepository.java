package edu.java.scrapper.domain.repository.jooq;

import edu.java.scrapper.domain.repository.LinkRepository;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.exception.DataBaseError;
import edu.java.scrapper.utils.mappers.LinkMapper;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import static edu.java.scrapper.domain.jooq.tables.Link.LINK;

@RequiredArgsConstructor
public class JOOQLinkRepository implements LinkRepository {

    private final DSLContext dslContext;
    private static final String LINK_CREATION_ERROR_MESSAGE = "Link was not created";
    private static final String INVALID_LINK_ERROR_MESSAGE = "Invalid Link";

    @Override
    public Link add(URI uri) {
        OffsetDateTime now = OffsetDateTime.now();
        dslContext.insertInto(LINK)
            .set(LINK.URL, uri.toString())
            .set(LINK.CREATED_AT, now)
            .set(LINK.LAST_UPDATED_AT, now)
            .execute();

        return getLinkByUri(uri).orElseThrow(() -> new DataBaseError(LINK_CREATION_ERROR_MESSAGE));
    }

    @Override
    public void remove(URI uri) {
        dslContext.deleteFrom(LINK)
            .where(LINK.URL.eq(uri.toString()))
            .execute();

    }

    @Override
    public void remove(long id) {
        dslContext.deleteFrom(LINK)
            .where(LINK.ID.eq(id))
            .execute();

    }

    @Override
    public Optional<Link> getLinkByUri(URI uri) {
        return dslContext.selectFrom(LINK)
            .where(LINK.URL.eq(uri.toString()))
            .fetchOptional()
            .map(LinkMapper::mapRecordToLink);
    }

    @Override
    public Optional<Link> getLinkById(long id) {
        return dslContext.selectFrom(LINK)
            .where(LINK.ID.eq(id))
            .fetchOptional()
            .map(LinkMapper::mapRecordToLink);
    }

    @Override
    public void updateLastUpdatedTime(long id) {
        dslContext.update(LINK)
            .set(LINK.LAST_UPDATED_AT, OffsetDateTime.now())
            .where(LINK.ID.eq(id))
            .execute();
    }

    @Override
    public List<Link> findAll() {
        return dslContext.selectFrom(LINK)
            .fetch()
            .map(LinkMapper::mapRecordToLink);
    }

    @Override
    public List<Link> findOldestLinks(int batchSize) {
        return dslContext.selectFrom(LINK)
            .orderBy(LINK.LAST_UPDATED_AT.asc())
            .limit(batchSize)
            .fetch()
            .map(LinkMapper::mapRecordToLink);
    }
}


