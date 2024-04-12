package edu.java.scrapper.service.jpa;

import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.domain.repository.jpa.JPALinkRepository;
import edu.java.scrapper.dto.entity.Link;
import edu.java.scrapper.updater.Updater;
import edu.java.scrapper.updater.UpdaterHolder;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("kafka")
public class JPALinkUpdaterKafkaTest extends IntegrationEnvironment {
    @Autowired
    private JPALinkRepository linkRepository;

    @Autowired
    private JPALinkUpdater jpaLinkUpdater;

    @MockBean
    private UpdaterHolder updaterHolder;

    @MockBean
    private BotClient botClient;

    @MockBean
    private Updater updater;


    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jpa");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Проверка работы обновлений")
    public void testUpdate() {
        for (int i = 0; i < 20; i++) {
            Link link = new Link();
            link.setUri(URI.create("https://example.com/" + i));
            link.setCreatedAt(OffsetDateTime.now().minusDays(1));
            link.setLastUpdatedAt(OffsetDateTime.now().minusDays(1));
            linkRepository.save(link);
        }


        when(updaterHolder.getUpdaterByHost(any())).thenReturn(Optional.of(updater));
        when(updater.getUpdateMessage(any())).thenReturn(String.valueOf(Optional.of("message")));


        ReflectionTestUtils.setField(jpaLinkUpdater, "batchSize", 10);


        int updatedCount = jpaLinkUpdater.update();


        assertEquals(10, updatedCount);
    }
}
