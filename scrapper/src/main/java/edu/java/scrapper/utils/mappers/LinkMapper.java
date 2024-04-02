package edu.java.scrapper.utils.mappers;

import edu.java.scrapper.domain.jooq.tables.records.LinkRecord;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.exception.InvalidLinkException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LinkMapper {

    private static final String INVALID_LINK_ERROR_MESSAGE = "Invalid Link";

    public static Link mapRecordToLink(LinkRecord result) {
        Link link = new Link();
        link.setId(result.getId());
        try {
            link.setUri(new URI(result.getUrl()));
        } catch (URISyntaxException e) {
            throw new InvalidLinkException(INVALID_LINK_ERROR_MESSAGE);
        }
        link.setCreatedAt(result.getCreatedAt());
        link.setLastUpdatedAt(result.getLastUpdatedAt());
        return link;
    }

    public static Link mapRowToLink(ResultSet rs) throws SQLException {
        Link link = new Link();
        link.setId(rs.getLong("id"));
        try {
            link.setUri(new URI(rs.getString("url")));
        } catch (URISyntaxException e) {
            throw new InvalidLinkException(INVALID_LINK_ERROR_MESSAGE);
        }
        link.setCreatedAt(convertTimestampToOffsetDateTime(rs.getTimestamp("created_at")));
        link.setLastUpdatedAt(convertTimestampToOffsetDateTime(rs.getTimestamp("last_updated_at")));
        return link;
    }

    private static OffsetDateTime convertTimestampToOffsetDateTime(Timestamp timestamp) {
        return (timestamp != null) ? timestamp.toInstant().atOffset(ZoneOffset.UTC) : null;
    }
}
