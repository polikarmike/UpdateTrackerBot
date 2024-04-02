package edu.java.bot.commands.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.common.dto.responses.LinkResponse;
import edu.java.common.dto.responses.ListLinksResponse;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.List;

public class ListCommandTest {
    @Test
    @DisplayName("Проверка выполнения команды /list")
    public void testExecuteListCommand() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        URI exampleUri = URI.create("http://example.com");
        ListLinksResponse response = new ListLinksResponse(List.of(new LinkResponse(1L, exampleUri)), 1);
        Mockito.when(scrapperClient.getAllLinks(anyLong())).thenReturn(response);

        ListCommand listCommand = new ListCommand(scrapperClient);
        Update update = Mockito.mock(Update.class);
        Mockito.when(update.message()).thenReturn(Mockito.mock(Message.class));
        Mockito.when(update.message().chat()).thenReturn(Mockito.mock(Chat.class));
        Mockito.when(update.message().chat().id()).thenReturn(12345L);

        // When
        String result = listCommand.execute(update);

        // Then
        assertEquals("Вот отслеживаемые ссылки:\n\n- http://example.com\n", result);
    }

    @Test
    @DisplayName("Проверка получения имени команды")
    public void testGetName() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.when(scrapperClient.getAllLinks(anyLong())).thenReturn(null);

        ListCommand listCommand = new ListCommand(scrapperClient);

        // When
        String name = listCommand.getName();

        // Then
        assertEquals("/list", name);
    }

    @Test
    @DisplayName("Проверка получения описания команды")
    public void testGetDescription() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.when(scrapperClient.getAllLinks(anyLong())).thenReturn(null);

        ListCommand listCommand = new ListCommand(scrapperClient);

        // When
        String description = listCommand.getDescription();

        // Then
        assertEquals("Отобразить список отслеживаемых ссылок", description);
    }
}
