package edu.java.bot.commands.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import edu.java.bot.client.scrapper.ScrapperClient;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;

public class UntrackCommandTest {
    @Test
    @DisplayName("Проверка выполнения команды /untrack")
    public void testExecuteUntrackCommand() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.doNothing().when(scrapperClient).removeLink(anyLong(), any(URI.class));

        UntrackCommand untrackCommand = new UntrackCommand(scrapperClient);
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Mockito.when(update.message()).thenReturn(message);
        Mockito.when(message.chat()).thenReturn(Mockito.mock(Chat.class));
        Mockito.when(message.chat().id()).thenReturn(12345L);
        Mockito.when(message.text()).thenReturn("/untrack https://example.com");

        // When
        String response = untrackCommand.execute(update);

        // Then
        assertEquals("Ссылка успешно удалена: https://example.com", response);
    }

    @Test
    @DisplayName("Проверка получения имени команды")
    public void testGetName() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.when(scrapperClient.getAllLinks(anyLong())).thenReturn(null);

        UntrackCommand untrackCommand = new UntrackCommand(scrapperClient);

        // When
        String name = untrackCommand.getName();

        // Then
        assertEquals("/untrack", name);
    }

    @Test
    @DisplayName("Проверка получения описания команды")
    public void testGetDescription() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.when(scrapperClient.getAllLinks(anyLong())).thenReturn(null);

        UntrackCommand untrackCommand = new UntrackCommand(scrapperClient);

        // When
        String description = untrackCommand.getDescription();

        // Then
        assertEquals("Прекратить отслеживание ссылки", description);
    }
}
