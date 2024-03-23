package edu.java.scrapper.service;

import edu.java.scrapper.domain.repository.ChatLinkRepository;
import edu.java.scrapper.domain.repository.ChatRepository;
import edu.java.scrapper.dto.entity.Chat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TgChatServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ChatLinkRepository chatLinkRepository;
    @Mock
    private LinkService linkService;

    @InjectMocks
    private TgChatService tgChatService;



    @Test
    public void testRegister() {
        long tgChatId = 123L;
        when(chatRepository.getById(tgChatId)).thenReturn(Optional.empty());

        tgChatService.register(tgChatId);

        verify(chatRepository, times(1)).add(any(Chat.class));
    }

    @Test
    public void testUnregister() {
        long tgChatId = 123L;
        Chat chat = new Chat();
        chat.setId(tgChatId);
        chat.setCreatedAt(LocalDateTime.now().atOffset(ZoneOffset.UTC));

        when(chatRepository.getById(tgChatId)).thenReturn(Optional.of(chat));
        when(chatLinkRepository.getLinkIdsByChatId(anyLong())).thenReturn(Arrays.asList(1L, 2L, 3L));

        tgChatService.unregister(tgChatId);

        verify(linkService, times(3)).remove(anyLong(), anyLong());
        verify(chatRepository, times(1)).remove(tgChatId);
    }
}
