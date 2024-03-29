package edu.java.scrapper.configuration;

import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.domain.repository.jdbc.JDBCChatLinkRepository;
import edu.java.scrapper.domain.repository.jdbc.JDBCChatRepository;
import edu.java.scrapper.domain.repository.jdbc.JDBCLinkRepository;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.TgChatService;
import edu.java.scrapper.service.jdbc.JDBCLinkService;
import edu.java.scrapper.service.jdbc.JDBCLinkUpdater;
import edu.java.scrapper.service.jdbc.JDBCTgChatService;
import edu.java.scrapper.updater.UpdaterHolder;
import edu.java.scrapper.utils.linkverifier.LinkVerifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
public class JDBCAccessConfig {
    @Bean
    public JDBCLinkService linkService(JDBCLinkRepository linkRepository, JDBCChatRepository chatRepository,
        JDBCChatLinkRepository chatLinkRepository, LinkVerifier linkVerifier) {
        return new JDBCLinkService(linkRepository, chatRepository, chatLinkRepository, linkVerifier);
    }

    @Bean
    public TgChatService chatService(JDBCChatRepository chatRepository, JDBCChatLinkRepository chatLinkRepository,
        LinkService linkService) {
        return new JDBCTgChatService(chatRepository, chatLinkRepository, linkService);
    }

    @Bean
    public LinkUpdater linkUpdater(LinkService linkService,
        UpdaterHolder updaterHolder, BotClient botClient) {
        return new JDBCLinkUpdater(linkService, updaterHolder, botClient);
    }

    @Bean
    public JDBCChatRepository jdbcChatRepository(JdbcTemplate jdbcTemplate) {
        return new JDBCChatRepository(jdbcTemplate);
    }

    @Bean
    public JDBCLinkRepository jdbcLinkRepository(JdbcTemplate jdbcTemplate) {
        return new JDBCLinkRepository(jdbcTemplate);
    }

    @Bean
    public JDBCChatLinkRepository jdbcChatLinkRepository(JdbcTemplate jdbcTemplate) {
        return new JDBCChatLinkRepository(jdbcTemplate);
    }

}
