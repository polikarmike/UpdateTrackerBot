package edu.java.scrapper.domain.repository.jooq;

import edu.java.scrapper.IntegrationEnvironment;
import edu.java.scrapper.dto.entity.Chat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JOOQChatRepositoryTest extends IntegrationEnvironment {
    @Autowired
    private JOOQChatRepository chatRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.database-access-type", () -> "jooq");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Добавление чата")
    void addTest() {
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));

        Chat existingChat = chatRepository.add(chat);

        assertEquals(chat.getId(), existingChat.getId());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Удаление чата")
    void removeTest() {
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(chat);

        Long id = chat.getId();

        chatRepository.remove(id);

        assertTrue(chatRepository.getById(id).isEmpty());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение чата по ID")
    void getByIdTest() {
        Chat chat = new Chat();
        chat.setId(12345L);
        chat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(chat);

        Long id = chat.getId();

        Optional<Chat> retrievedChat = chatRepository.getById(id);

        assertNotNull(retrievedChat.orElse(null));
        assertEquals(id, retrievedChat.get().getId());
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Получение всех чатов")
    void findAllTest() {
        Chat chat1 = new Chat();
        chat1.setId(1L);
        chat1.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(chat1);

        Chat chat2 = new Chat();
        chat2.setId(2L);
        chat2.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));
        chatRepository.add(chat2);

        List<Chat> chats = chatRepository.findAll();

        assertEquals(2, chats.size());

        System.out.println(chats.toString());
        assertTrue(chats.stream().anyMatch(chat -> chat.getId().equals(chat1.getId())));
        assertTrue(chats.stream().anyMatch(chat -> chat.getId().equals(chat2.getId())));
    }
}
