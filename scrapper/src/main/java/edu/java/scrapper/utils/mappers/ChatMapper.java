package edu.java.scrapper.utils.mappers;

import edu.java.scrapper.domain.jooq.tables.records.ChatRecord;
import edu.java.scrapper.dto.entity.Chat;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ChatMapper {
    public static Chat mapRowToChat(ResultSet rs) throws SQLException {
        Chat chat = new Chat();
        chat.setId(rs.getLong("id"));
        OffsetDateTime createdAt = rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
        chat.setCreatedAt(createdAt);
        return chat;
    }

    public static Chat mapRecordToChat(ChatRecord result) {
        Chat chat = new Chat();
        chat.setId(result.getId());
        chat.setCreatedAt(result.getCreatedAt());
        return chat;
    }
}
