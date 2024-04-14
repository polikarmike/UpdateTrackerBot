package edu.java.bot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.commands.CommandHolder;
import edu.java.bot.processor.SimpleUserMessageProcessor;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateTrackerBotTest {

    private TelegramBot telegramBot;
    private SimpleUserMessageProcessor simpleUserMessageProcessor;
    private CommandHolder commandHolder;
    private UpdateTrackerBot updateTrackerBot;


    @BeforeEach
    void setUp() {
        // Given
        telegramBot = Mockito.mock(TelegramBot.class);
        simpleUserMessageProcessor = Mockito.mock(SimpleUserMessageProcessor.class);
        commandHolder = Mockito.mock(CommandHolder.class);
        updateTrackerBot = new UpdateTrackerBot(telegramBot, simpleUserMessageProcessor, commandHolder);
    }

    @Test
    @DisplayName("Проверка выполнения")
     void testExecute(){
     // Given
     SendMessage sendMessageRequest=new SendMessage(123, "Test message");

     // When
     updateTrackerBot.execute(sendMessageRequest);

     // Then
     verify(telegramBot).execute(sendMessageRequest);
     }

     @Test
     @DisplayName("Проверка обработки")
     void testProcess(){
     // Given
     SendMessage response=new SendMessage(123, "Response");
     Update update=Mockito.mock(Update.class);
     Message message=Mockito.mock(Message.class);
     when(update.message()).thenReturn(message);
     when(simpleUserMessageProcessor.process(update)).thenReturn(response);

     // When
     updateTrackerBot.process(Collections.singletonList(update));

     // Then
     ArgumentCaptor<SendMessage>sendMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
     verify(telegramBot).execute(sendMessageCaptor.capture());
     assertEquals(response, sendMessageCaptor.getValue());
    }

    @Test
    @DisplayName("Проверка запуска")
    void testStart() {
        // When
        updateTrackerBot.start();

        // Then
        verify(telegramBot).setUpdatesListener(updateTrackerBot);
    }

    @Test
    @DisplayName("Проверка закрытия")
    void testClose() {
        // When
        updateTrackerBot.close();

        // Then
        verify(telegramBot).removeGetUpdatesListener();
    }
}


