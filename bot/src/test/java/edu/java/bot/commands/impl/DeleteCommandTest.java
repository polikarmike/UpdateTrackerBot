package edu.java.bot.commands.impl;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.client.scrapper.ScrapperClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;

public class DeleteCommandTest {
    @Test
    @DisplayName("Проверка выполнения команды /delete")
    public void testExecuteDeleteCommand() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.when(scrapperClient.deleteChat(anyLong())).thenReturn(null);

        DeleteCommand deleteCommand = new DeleteCommand(scrapperClient);
        Update update = Mockito.mock(Update.class);
        Mockito.when(update.message()).thenReturn(Mockito.mock(Message.class));
        Mockito.when(update.message().chat()).thenReturn(Mockito.mock(Chat.class));
        Mockito.when(update.message().chat().id()).thenReturn(12345L);

        // When
        String response = deleteCommand.execute(update);

        // Then
        assertEquals("Чат успешно удален.", response);
    }

    @Test
    @DisplayName("Проверка получения имени команды")
    public void testGetName() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.when(scrapperClient.deleteChat(anyLong())).thenReturn(null);

        DeleteCommand deleteCommand = new DeleteCommand(scrapperClient);

        // When
        String name = deleteCommand.getName();

        // Then
        assertEquals("/delete", name);
    }

    @Test
    @DisplayName("Проверка получения описания команды")
    public void testGetDescription() {
        // Given
        ScrapperClient scrapperClient = Mockito.mock(ScrapperClient.class);
        Mockito.when(scrapperClient.deleteChat(anyLong())).thenReturn(null);

        DeleteCommand deleteCommand = new DeleteCommand(scrapperClient);

        // When
        String description = deleteCommand.getDescription();

        // Then
        assertEquals("Прекратить работу и удалить данные", description);
    }
}
