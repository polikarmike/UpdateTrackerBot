package edu.java.scrapper.domain.repository.jdbc;

import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.dto.entity.Chat;
import edu.java.scrapper.dto.entity.Link;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JDBCChatLinkRepositoryTest extends IntegrationEnvironment {
    @Autowired
    private JDBCChatRepository chatRepository;
    @Autowired
    private JDBCLinkRepository linkRepository;
    @Autowired
    private JDBCChatLinkRepository chatLinkRepository;
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jdbc");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Добавление связи между чатом и ссылкой")
    void addTest()
        throws URISyntaxException {

        Long chatId = 1L;

        Chat exampleChat = new Chat();
        exampleChat.setId(chatId);
        exampleChat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(exampleChat);

        URI exampleURI = new URI("https://example.com");
        linkRepository.add(exampleURI);
        Long linkId = linkRepository.getLinkByUri(exampleURI).get().getId();

        chatLinkRepository.add(chatId, linkId);

        List<Long> retrievedLinkIds = chatLinkRepository.getLinkIdsByChatId(1L);
        assertTrue(retrievedLinkIds.contains(linkId));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление связи между чатом и ссылкой")
    void removeTest()
        throws URISyntaxException {

        Long chatId = 1L;

        Chat exampleChat = new Chat();
        exampleChat.setId(chatId);
        exampleChat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(exampleChat);

        URI exampleURI = new URI("https://example.com");
        linkRepository.add(exampleURI);
        Long linkId = linkRepository.getLinkByUri(exampleURI).get().getId();

        chatLinkRepository.add(chatId, linkId);
        chatLinkRepository.remove(chatId, linkId);

        List<Long> retrievedLinkIds = chatLinkRepository.getLinkIdsByChatId(chatId);

        Assertions.assertFalse(retrievedLinkIds.contains(linkId));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение идентификаторов ссылок по идентификатору чата")
    void getLinkIdsByChatIdTest()
        throws URISyntaxException {

        Long chatId = 1L;

        Chat exampleChat = new Chat();
        exampleChat.setId(chatId);
        exampleChat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(exampleChat);

        URI exampleURI1 = new URI("https://example.com");
        linkRepository.add(exampleURI1);
        Long linkId1 = linkRepository.getLinkByUri(exampleURI1).get().getId();

        URI exampleURI2 = new URI("https://example2.com");
        linkRepository.add(exampleURI2);
        Long linkId2 = linkRepository.getLinkByUri(exampleURI2).get().getId();

        chatLinkRepository.add(chatId, linkId1);
        chatLinkRepository.add(chatId, linkId2);

        List<Long> retrievedLinkIds = chatLinkRepository.getLinkIdsByChatId(chatId);

        Assertions.assertEquals(2, retrievedLinkIds.size());
        assertTrue(retrievedLinkIds.contains(linkId1));
        assertTrue(retrievedLinkIds.contains(linkId2));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение идентификаторов чатов по идентификатору ссылки")
    void getChatIdsByLinkIdTest()
        throws URISyntaxException {

        Long chatId1 = 1L;

        Chat exampleChat1 = new Chat();
        exampleChat1.setId(chatId1);
        exampleChat1.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(exampleChat1);

        Long chatId2 = 2L;

        Chat exampleChat2 = new Chat();
        exampleChat2.setId(chatId2);
        exampleChat2.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(exampleChat2);

        URI exampleURI = new URI("https://example.com");
        linkRepository.add(exampleURI);
        Long linkId = linkRepository.getLinkByUri(exampleURI).get().getId();

        chatLinkRepository.add(chatId1, linkId);
        chatLinkRepository.add(chatId2, linkId);

        List<Long> retrievedChatIds = chatLinkRepository.getChatIdsByLinkId(linkId);

        Assertions.assertEquals(2, retrievedChatIds.size());
        assertTrue(retrievedChatIds.contains(chatId1));
        assertTrue(retrievedChatIds.contains(chatId2));
    }


    @Test
    @Transactional
    @Rollback
    @DisplayName("Проверка существования связных идентификатора чата и идентификатора ссылки")
    void existChatIdLinkId()
        throws URISyntaxException {

        Long chatId = 1L;

        Chat exampleChat = new Chat();
        exampleChat.setId(chatId);
        exampleChat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        Chat addedChat = chatRepository.add(exampleChat);

        URI exampleURI = new URI("https://example.com");
        Link addedLink = linkRepository.add(exampleURI);


        chatLinkRepository.add(addedChat.getId(), addedLink.getId());


        assertTrue(chatLinkRepository.exists(addedChat.getId(), addedLink.getId()));
    }
}
