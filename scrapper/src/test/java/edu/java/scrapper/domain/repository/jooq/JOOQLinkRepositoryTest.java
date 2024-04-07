package edu.java.scrapper.domain.repository.jooq;

import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.domain.repository.LinkRepository;
import edu.java.scrapper.dto.entity.Link;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JOOQLinkRepositoryTest extends IntegrationEnvironment {
    @Autowired
    private LinkRepository linkRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jooq");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Добавление ссылки")
    void addTest() throws URISyntaxException {
        URI exampleURI = new URI("https://example.com");

        Link addedLink = linkRepository.add(exampleURI);

        assertEquals(exampleURI, addedLink.getUri());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление всех ссылок")
    void removeTest() throws URISyntaxException {
        URI exampleURI = new URI("https://example.com");
        Link addedLink = linkRepository.add(exampleURI);

        linkRepository.remove(exampleURI);

        assertTrue(linkRepository.getLinkById(addedLink.getId()).isEmpty());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение всех ссылок")
    void findAllTest() throws URISyntaxException {
        URI exampleURI1 = new URI("https://example1.com");
        Link addedLink1 = linkRepository.add(exampleURI1);

        URI exampleURI2 = new URI("https://example2.com");
        Link addedLink2 = linkRepository.add(exampleURI2);

        List<Link> links = linkRepository.findAll();

        assertEquals(2, links.size());

        assertTrue(links.contains(addedLink1));
        assertTrue(links.contains(addedLink2));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение ссылки по ID")
    void getLinkByIdTest() throws URISyntaxException {
        URI exampleURI = new URI("https://example.com");
        Link addedLink = linkRepository.add(exampleURI);

        Optional<Link> retrievedLink = linkRepository.getLinkById(addedLink.getId());

        assertTrue(retrievedLink.isPresent());
        assertEquals(addedLink, retrievedLink.get());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение ссылки по URI")
    void getLinkByUriTest() throws URISyntaxException {
        URI exampleURI = new URI("https://example.com");
        Link addedLink = linkRepository.add(exampleURI);

        Optional<Link> retrievedLink = linkRepository.getLinkByUri(exampleURI);

        assertTrue(retrievedLink.isPresent());
        assertEquals(addedLink, retrievedLink.get());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Обновление времени последней проверки ссылки")
    void updateLastUpdatedTimeTest() throws URISyntaxException {
        URI exampleURI = new URI("https://example.com");
        Link addedLink = linkRepository.add(exampleURI);

        OffsetDateTime initialLastUpdatedAt = addedLink.getLastUpdatedAt();

        linkRepository.updateLastUpdatedTime(addedLink.getId());

        Link updatedLink = linkRepository.getLinkById(addedLink.getId()).orElse(null);

        assertNotNull(updatedLink);
        assertTrue(initialLastUpdatedAt.isBefore(updatedLink.getLastUpdatedAt()));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Поиск старых ссылок")
    void findOldestLinksTest() throws URISyntaxException {
        int batchSize = 2;

        URI exampleURI1 = new URI("https://example1.com");
        Link addedLink1 = linkRepository.add(exampleURI1);

        URI exampleURI2 = new URI("https://example2.com");
        Link addedLink2 = linkRepository.add(exampleURI2);

        URI exampleURI3 = new URI("https://example3.com");
        Link addedLink3 = linkRepository.add(exampleURI3);

        List<Link> outdatedLinks = linkRepository.findOldestLinks(batchSize);

        assertTrue(outdatedLinks.contains(addedLink1));
        assertTrue(outdatedLinks.contains(addedLink2));
    }
}
