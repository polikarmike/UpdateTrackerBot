package edu.java.scrapper.domain.repository.jdbc;

import edu.java.scrapper.domain.repository.ChatLinkRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class JDBCChatLinkRepository implements ChatLinkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int add(Long chatId, Long linkId) {
        return jdbcTemplate.update("INSERT INTO Link_Chat (link_id, chat_id) VALUES (?, ?)",
            linkId, chatId);
    }

    @Override
    public int remove(Long chatId, Long linkId) {
        return jdbcTemplate.update("DELETE FROM Link_Chat WHERE chat_id = ? AND link_id = ?", chatId, linkId);
    }

    @Override
    public List<Long> getLinkIdsByChatId(Long chatId) {
        return jdbcTemplate.queryForList("SELECT link_id FROM Link_Chat WHERE chat_id = ?", Long.class, chatId);
    }

    @Override
    public List<Long> getChatIdsByLinkId(Long linkId) {
        return jdbcTemplate.queryForList("SELECT chat_id FROM Link_Chat WHERE link_id = ?", Long.class, linkId);
    }

    @Override
    public boolean exists(Long chatId, Long linkId) {
        String sql = "SELECT COUNT(*) FROM Link_Chat WHERE chat_id = ? AND link_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId, linkId);
        return count != null && count > 0;
    }

    @Override
    public int cleanupUnusedLinks() {
        String sql = "DELETE FROM Link WHERE id NOT IN (SELECT link_id FROM Link_Chat)";
        return jdbcTemplate.update(sql);
    }
}
