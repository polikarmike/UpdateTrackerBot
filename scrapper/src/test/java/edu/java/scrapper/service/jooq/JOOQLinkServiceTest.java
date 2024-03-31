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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JOOQLinkServiceTest extends IntegrationEnvironment{
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
    @DisplayName("Добавление ссылки в чат")
    public void testAdd() {
        long tgChatId = 123L;
        URI uri = URI.create("http://example.com");

        tgChatService.register(tgChatId);

        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link addedLink = linkService.add(tgChatId, uri);

        Assertions.assertTrue(linkRepository.getLinkByUri(uri).isPresent());
        Assertions.assertTrue(chatLinkRepository.exists(tgChatId, addedLink.getId()));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление ссылки из чата по ссылке")
    public void testRemoveByUri() {
        long tgChatId = 123L;
        URI uri = URI.create("http://example.com");

        tgChatService.register(tgChatId);

        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link addedLink = linkService.add(tgChatId, uri);

        Assertions.assertTrue(linkRepository.getLinkByUri(uri).isPresent());
        Assertions.assertTrue(chatLinkRepository.exists(tgChatId, addedLink.getId()));

        linkService.remove(tgChatId, addedLink.getUri());
        Assertions.assertFalse(chatLinkRepository.exists(tgChatId, addedLink.getId()));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление ссылки из чата по индефикатору")
    public void testRemoveById() {
        long tgChatId = 123L;
        URI uri = URI.create("http://example.com");

        tgChatService.register(tgChatId);

        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link addedLink = linkService.add(tgChatId, uri);
        long linkId = addedLink.getId();

        Assertions.assertTrue(linkRepository.getLinkById(linkId).isPresent());
        Assertions.assertTrue(chatLinkRepository.exists(tgChatId, addedLink.getId()));

        linkService.remove(tgChatId, linkId);
        Assertions.assertFalse(chatLinkRepository.exists(tgChatId, linkId));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение всех ссылок чата")
    public void testListAll() {
        long tgChatId = 123L;
        URI uri1 = URI.create("http://example1.com");
        URI uri2 = URI.create("http://example2.com");

        tgChatService.register(tgChatId);

        when(linkVerifier.checkLink(uri1)).thenReturn(true);
        when(linkVerifier.checkLink(uri2)).thenReturn(true);
        Link addedLink1 = linkService.add(tgChatId, uri1);
        Link addedLink2 = linkService.add(tgChatId, uri2);

        Collection<Link> links = linkService.listAll(tgChatId);

        Assertions.assertEquals(2, links.size());
        Assertions.assertTrue(links.contains(addedLink1));
        Assertions.assertTrue(links.contains(addedLink2));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Очистка неиспользуемых ссылок")
    public void testCleanupUnusedLinks() {
        long tgChatId = 123L;
        URI uri1 = URI.create("http://example1.com");
        URI uri2 = URI.create("http://example2.com");

        tgChatService.register(tgChatId);

        when(linkVerifier.checkLink(uri1)).thenReturn(true);
        when(linkVerifier.checkLink(uri2)).thenReturn(true);
        Link addedLink1 = linkService.add(tgChatId, uri1);
        Link addedLink2 = linkService.add(tgChatId, uri2);

        Assertions.assertTrue(chatLinkRepository.exists(tgChatId, addedLink1.getId()));
        Assertions.assertTrue(chatLinkRepository.exists(tgChatId, addedLink2.getId()));

        tgChatService.unregister(tgChatId);

        Assertions.assertFalse(chatLinkRepository.exists(tgChatId, addedLink1.getId()));
        Assertions.assertFalse(chatLinkRepository.exists(tgChatId, addedLink2.getId()));

        Assertions.assertTrue(linkRepository.getLinkById(addedLink1.getId()).isPresent());
        Assertions.assertTrue(linkRepository.getLinkById(addedLink2.getId()).isPresent());

        int cleanupCount = linkService.cleanUpUnusedLink();

        Assertions.assertEquals(2, cleanupCount);

        Assertions.assertFalse(linkRepository.getLinkById(addedLink1.getId()).isPresent());
        Assertions.assertFalse(linkRepository.getLinkById(addedLink2.getId()).isPresent());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Поиск старейших ссылок")
    public void testFindOldestLinks() {
        long tgChatId = 123L;
        URI uri1 = URI.create("http://example1.com");
        URI uri2 = URI.create("http://example2.com");
        URI uri3 = URI.create("http://example3.com");

        tgChatService.register(tgChatId);

        when(linkVerifier.checkLink(uri1)).thenReturn(true);
        when(linkVerifier.checkLink(uri2)).thenReturn(true);
        when(linkVerifier.checkLink(uri3)).thenReturn(true);
        Link addedLink1 = linkService.add(tgChatId, uri1);
        Link addedLink2 = linkService.add(tgChatId, uri2);
        Link addedLink3 = linkService.add(tgChatId, uri3);

        int batchSize = 2;

        List<Link> oldestLinks = linkService.findOldestLinks(batchSize);


        Assertions.assertEquals(2, oldestLinks.size());
        Assertions.assertTrue(oldestLinks.contains(addedLink1));
        Assertions.assertTrue(oldestLinks.contains(addedLink2));
        Assertions.assertFalse(oldestLinks.contains(addedLink3));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение идентификаторов чатов по идентификатору ссылки")
    public void testGetChatIdsByLinkId() {
        long tgChatId1 = 123L;
        long tgChatId2 = 456L;
        long tgChatId3 = 789L;

        URI uri = URI.create("http://example.com");

        tgChatService.register(tgChatId1);
        tgChatService.register(tgChatId2);
        tgChatService.register(tgChatId3);

        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link addedLink1 = linkService.add(tgChatId1, uri);
        Link addedLink2 = linkService.add(tgChatId2, uri);
        Link addedLink3 = linkService.add(tgChatId3, uri);

        List<Long> chatIds = linkService.getChatIdsByLinkId(addedLink1.getId());

        Assertions.assertEquals(3, chatIds.size());
        Assertions.assertTrue(chatIds.contains(tgChatId1));
        Assertions.assertTrue(chatIds.contains(tgChatId2));
        Assertions.assertTrue(chatIds.contains(tgChatId3));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Обновление времени последнего обновления ссылки")
    public void testUpdateLastUpdatedTime() {
        long tgChatId = 123L;
        URI uri = URI.create("http://example.com");

        tgChatService.register(tgChatId);

        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link addedLink = linkService.add(tgChatId, uri);
        OffsetDateTime timeBefore = addedLink.getLastUpdatedAt();

        linkService.updateLastUpdatedTime(addedLink.getId());

        Link updateLink = linkRepository.getLinkById(addedLink.getId()).get();
        OffsetDateTime timeAfter = updateLink.getLastUpdatedAt();

        Assertions.assertTrue(timeBefore.isBefore(timeAfter));
    }
}
