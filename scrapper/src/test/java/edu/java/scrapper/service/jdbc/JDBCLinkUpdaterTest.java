package edu.java.scrapper.service.jdbc;

import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.client.bot.BotClient;
import edu.java.scrapper.domain.repository.jdbc.JDBCChatLinkRepository;
import edu.java.scrapper.domain.repository.jdbc.JDBCChatRepository;
import edu.java.scrapper.domain.repository.jdbc.JDBCLinkRepository;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.LinkUpdater;
import edu.java.scrapper.service.TgChatService;
import edu.java.scrapper.updater.Updater;
import edu.java.scrapper.updater.UpdaterHolder;
import edu.java.scrapper.utils.linkverifier.LinkVerifier;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDBCLinkUpdaterTest extends IntegrationEnvironment{
    @Autowired
    private JDBCChatRepository chatRepository;
    @Autowired
    private JDBCLinkRepository linkRepository;
    @Autowired
    private JDBCChatLinkRepository chatLinkRepository;
    @Autowired
    private TgChatService tgChatService;
    @Autowired
    private LinkService linkService;
    @Autowired
    private LinkUpdater linkUpdater;
    @MockBean
    private LinkVerifier linkVerifier;
    @MockBean
    private UpdaterHolder updaterHolder;
    @MockBean
    private BotClient botClient;
    @MockBean
    private Updater updater;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jdbc");
    }


    @Test
    @Transactional
    @Rollback
    @DisplayName("Проверка работы обновлений")
    public void testUpdate() {
        long tgChatId = 123L;
        tgChatService.register(tgChatId);
        when(linkVerifier.checkLink(any())).thenReturn(true);

        for (int i = 0; i < 20; i++) {
            linkService.add(tgChatId, URI.create("https://example.com/" + i));
        }

        when(updaterHolder.getUpdaterByHost(any())).thenReturn(Optional.of(updater));
        when(updater.getUpdateMessage(any())).thenReturn(String.valueOf(Optional.of("message")));

        ReflectionTestUtils.setField(linkUpdater, "batchSize", 10);

        int updatedCount = linkUpdater.update();

        assertEquals(10, updatedCount);
    }
}

