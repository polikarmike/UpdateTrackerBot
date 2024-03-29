package edu.java.scrapper.service.jooq;

import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.domain.repository.jooq.JOOQChatLinkRepository;
import edu.java.scrapper.domain.repository.jooq.JOOQChatRepository;
import edu.java.scrapper.domain.repository.jooq.JOOQLinkRepository;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.TgChatService;
import edu.java.scrapper.utils.linkverifier.LinkVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JOOQTgChatServiceTest  extends IntegrationEnvironment {
    @Autowired
    private JOOQChatRepository chatRepository;
    @Autowired
    private JOOQLinkRepository linkRepository;
    @Autowired
    private JOOQChatLinkRepository chatLinkRepository;
    @Autowired
    private TgChatService tgChatService;
    @Autowired
    private LinkService linkService;
    @MockBean
    private LinkVerifier linkVerifier;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jooq");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Регистрация чата с помощью сервиса")
    void registerTest() {
        long tgChatId = 123L;
        tgChatService.register(tgChatId);
        Assertions.assertTrue(chatRepository.getById(tgChatId).isPresent());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление чата без ссылок")
    void uregisterTest_empty() {

        long tgChatId = 123L;

        tgChatService.register(tgChatId);

        Assertions.assertTrue(chatRepository.getById(tgChatId).isPresent());

        tgChatService.unregister(tgChatId);

        Assertions.assertTrue(chatRepository.getById(tgChatId).isEmpty());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление чата со ссылками")
    void uregisterTest_notEmpty() {
        long tgChatId = 123L;
        URI uri = URI.create("example.com");

        tgChatService.register(tgChatId);

        Assertions.assertTrue(chatRepository.getById(tgChatId).isPresent());

        when(linkVerifier.checkLink(uri)).thenReturn(Boolean.TRUE);
        Link link = linkService.add(tgChatId, uri);

        Assertions.assertTrue(chatLinkRepository.exists(tgChatId, link.getId()));

        tgChatService.unregister(tgChatId);

        Assertions.assertTrue(chatRepository.getById(tgChatId).isEmpty());
        Assertions.assertFalse(chatLinkRepository.exists(tgChatId, link.getId()));
    }
}
