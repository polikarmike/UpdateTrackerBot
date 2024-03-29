package edu.java.scrapper.service.jpa;

import java.util.Optional;
import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.domain.repository.jpa.JPAChatRepository;
import edu.java.scrapper.domain.repository.jpa.JPALinkRepository;
import edu.java.scrapper.dto.entity.Chat;
import edu.java.scrapper.exception.MissingChatException;
import edu.java.scrapper.exception.RepeatedRegistrationException;
import edu.java.scrapper.service.LinkService;
import edu.java.scrapper.service.TgChatService;
import edu.java.scrapper.utils.linkverifier.LinkVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JPATgChatServiceTest extends IntegrationEnvironment {

    @Autowired
    private JPAChatRepository chatRepository;
    @Autowired
    private JPALinkRepository linkRepository;
    @Autowired
    private LinkService linkService;
    @Autowired
    private TgChatService tgChatService;
    @MockBean
    private LinkVerifier linkVerifier;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jpa");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Регистрация чата")
    public void testRegisterChat_Success() {
        long tgChatId = 1L;

        tgChatService.register(tgChatId);

        Optional<Chat> savedChatOptional = chatRepository.findById(tgChatId);

        assertTrue(savedChatOptional.isPresent());
        assertEquals(tgChatId, savedChatOptional.get().getId());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Регистрация уже существующего чата")
    public void testRegisterChat_AlreadyExists() {
        long tgChatId = 1L;

        tgChatService.register(tgChatId);

        assertThrows(RepeatedRegistrationException.class, () -> tgChatService.register(tgChatId));
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление чата")
    public void testUnregisterChat_Success() {
        long tgChatId = 1L;

        tgChatService.register(tgChatId);
        tgChatService.unregister(tgChatId);

        Optional<Chat> deletedChatOptional = chatRepository.findById(tgChatId);

        assertFalse(deletedChatOptional.isPresent());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаления несуществующего чата")
    public void testUnregisterChat_NotFound() {
        long tgChatId = 1L;

        assertThrows(MissingChatException.class, () -> tgChatService.unregister(tgChatId));
    }
}
