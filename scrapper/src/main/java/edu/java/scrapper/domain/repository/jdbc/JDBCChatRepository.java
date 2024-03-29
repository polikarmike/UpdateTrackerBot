package edu.java.scrapper.domain.repository.jdbc;

import edu.java.scrapper.domain.repository.ChatRepository;
import edu.java.scrapper.dto.entity.Chat;
import edu.java.scrapper.exception.DataBaseError;
import edu.java.scrapper.utils.mappers.ChatMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
@Qualifier("JDBCChatRepository")
public class JDBCChatRepository implements ChatRepository {

    private final JdbcTemplate jdbcTemplate;
    private static final String CHAT_CREATION_ERROR_MESSAGE = "Chat was not created";

    @Override
    public Chat add(Chat chat) {
        jdbcTemplate.update("INSERT INTO Chat (id, created_at) VALUES (?, ?)",
            chat.getId(), chat.getCreatedAt());
        return getById(chat.getId()).orElseThrow(() -> new DataBaseError(CHAT_CREATION_ERROR_MESSAGE));
    }

    @Override
    public void remove(Long id) {
        jdbcTemplate.update("DELETE FROM Chat WHERE id = ?", id);
    }

    @Override
    public List<Chat> findAll() {
        return jdbcTemplate.query(
            "SELECT * FROM Chat",
            (rs, rowNum) -> ChatMapper.mapRowToChat(rs));
    }

    @Override
    public Optional<Chat> getById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT * FROM Chat WHERE id = ?",
                (rs, rowNum) -> Optional.of(ChatMapper.mapRowToChat(rs)),
                id
            );
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
