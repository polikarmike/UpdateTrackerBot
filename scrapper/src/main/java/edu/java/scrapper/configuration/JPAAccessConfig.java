package edu.java.scrapper.configuration;

import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.domain.repository.jpa.JPAChatRepository;
import edu.java.scrapper.domain.repository.jpa.JPALinkRepository;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.TgChatService;
import edu.java.scrapper.service.jpa.JPALinkService;
import edu.java.scrapper.service.jpa.JPALinkUpdater;
import edu.java.scrapper.service.jpa.JPATgChatService;
import edu.java.scrapper.updater.UpdaterHolder;
import edu.java.scrapper.utils.linkverifier.LinkVerifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JPAAccessConfig {
    @Bean
    public LinkService linkService(JPALinkRepository linkRepository, JPAChatRepository chatRepository,
        LinkVerifier linkVerifier) {
        return new JPALinkService(linkRepository, chatRepository, linkVerifier);
    }

    @Bean
    public TgChatService chatService(JPAChatRepository chatRepository, JPALinkRepository linkRepository) {
        return new JPATgChatService(chatRepository, linkRepository);
    }

    @Bean
    public LinkUpdater linkUpdater(LinkService linkService, UpdaterHolder updaterHolder,
        BotClient botClient) {
        return new JPALinkUpdater(linkService, updaterHolder, botClient);
    }

}
