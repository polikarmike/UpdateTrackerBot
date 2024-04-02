package edu.java.scrapper.service.jpa;

import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.domain.repository.jpa.JPAChatRepository;
import edu.java.scrapper.domain.repository.jpa.JPALinkRepository;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.utils.linkverifier.LinkVerifier;
import jakarta.persistence.EntityManager;
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
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JPALinkServiceTest extends IntegrationEnvironment {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JPALinkRepository linkRepository;

    @Autowired
    private JPAChatRepository chatRepository;

    @Autowired
    private JPALinkService linkService;

    @Autowired
    private JPATgChatService TgChatService;

    @MockBean
    private LinkVerifier linkVerifier;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jpa");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Добавление ссылки")
    public void testAddLink() {
        long tgChatId = 1L;
        TgChatService.register(tgChatId);

        URI uri = URI.create("https://example.com");
        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link result = linkService.add(tgChatId, uri);

        Assertions.assertTrue(linkRepository.existsByChatsIdAndId(tgChatId, result.getId()));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление ссылки по ссылке")
    public void testRemoveLinkByUri() {
        long tgChatId = 1L;
        URI uri = URI.create("https://example.com");

        when(linkVerifier.checkLink(uri)).thenReturn(true);
        TgChatService.register(1L);

        Link result = linkService.add(tgChatId, uri);
        linkService.remove(tgChatId, uri);

        Assertions.assertFalse(linkRepository.existsByChatsIdAndId(tgChatId, result.getId()));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление ссылки по идентификатору ссылки")
    public void testRemoveLinkById() {
        long tgChatId = 1L;
        TgChatService.register(tgChatId);

        URI uri = URI.create("https://example.com");
        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link result = linkService.add(tgChatId, uri);

        linkService.remove(tgChatId, result.getId());

        Assertions.assertFalse(linkRepository.existsByChatsIdAndId(tgChatId, result.getId()));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение всех ссылок")
    public void testListAllLinks() {
        long tgChatId = 1L;
        TgChatService.register(1L);

        URI uri1 = URI.create("https://example.com/1");
        URI uri2 = URI.create("https://example.com/2");

        when(linkVerifier.checkLink(uri1)).thenReturn(true);
        when(linkVerifier.checkLink(uri2)).thenReturn(true);

        Link result1 = linkService.add(tgChatId, uri1);
        Link result2 = linkService.add(tgChatId, uri2);

        Collection<Link> links = linkService.listAll(tgChatId);

        Assertions.assertTrue(links.contains(result1));
        Assertions.assertTrue(links.contains(result2));

    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение идентификаторов чатов по идентификатору ссылки")
    public void testGetChatIdsByLinkId() {
        long tgChatId1 = 1L;
        long tgChatId2 = 2L;

        URI uri = URI.create("https://example.com");

        TgChatService.register(tgChatId1);
        TgChatService.register(tgChatId2);

        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link result1 = linkService.add(tgChatId1, uri);
        Link result2 = linkService.add(tgChatId2, uri);

        List<Long> chatIds = linkService.getChatIdsByLinkId(result1.getId());

        Assertions.assertEquals(2, chatIds.size());
        Assertions.assertTrue(chatIds.contains(tgChatId1));
        Assertions.assertTrue(chatIds.contains(tgChatId2));
    }

    @Test
    @Transactional
    @DisplayName("Поиск старых ссылок")
    public void testFindOldestLinks() {
        int batchSize = 2;
        when(linkVerifier.checkLink(any())).thenReturn(true);
        for (long i = 1; i < batchSize + 3; i++) {
            URI uri = URI.create("https://example.com/" + i);
            TgChatService.register(i);
            linkService.add(i, uri);
        }

        List<Link> oldestLinks = linkService.findOldestLinks(batchSize);

        Assertions.assertEquals(batchSize, oldestLinks.size());
        Assertions.assertTrue(oldestLinks.stream().anyMatch(link -> link.getUri().toString().equals("https://example.com/1")));
        Assertions.assertTrue(oldestLinks.stream().anyMatch(link -> link.getUri().toString().equals("https://example.com/2")));
    }

    @Test
    @Transactional
    @DisplayName("Обновление времени последнего обновления")
    public void testUpdateLastUpdatedTime() throws InterruptedException {
        long tgChatId = 1L;
        TgChatService.register(tgChatId);

        URI uri = URI.create("https://example.com");
        when(linkVerifier.checkLink(uri)).thenReturn(true);
        Link link = linkService.add(tgChatId, uri);
        OffsetDateTime timeBefore = link.getLastUpdatedAt();

        Thread.sleep(1000);
        linkService.updateLastUpdatedTime(link.getId());
        OffsetDateTime timeAfter = link.getLastUpdatedAt();

        Assertions.assertTrue(timeBefore.isBefore(timeAfter));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление неиспользуемых ссылок")
    public void testCleanUpUnusedLink_Success() {
        URI uri1 = URI.create("https://example.com/1");
        URI uri2 = URI.create("https://example.com/2");
        Link link1 = new Link(uri1);
        Link link2 = new Link(uri2);

        linkRepository.saveAll(List.of(link1, link2));
        int deletedLinksCount = linkService.cleanUpUnusedLink();

        Assertions.assertEquals(2, deletedLinksCount);
        Assertions.assertEquals(0, linkRepository.count());
    }
}
