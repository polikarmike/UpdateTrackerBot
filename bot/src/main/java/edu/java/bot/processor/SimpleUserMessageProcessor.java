package edu.java.bot.processor;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.commands.Command;
import edu.java.bot.commands.CommandHolder;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SimpleUserMessageProcessor implements UserMessageProcessor {
    private final CommandHolder commandHolder;
    private static final String COMMAND_NOT_FOUND_MESSAGE = "Команда не найдена. Для получения помощи наберите /help.";

    public SendMessage process(Update update) {
        String messageText = update.message().text();

        String commandName = messageText.split(" ")[0];

        Optional<Command> optionalCommand = commandHolder.getCommandByName(commandName);
        String message = optionalCommand.map(command -> command.execute(update)).orElse(COMMAND_NOT_FOUND_MESSAGE);

        return new SendMessage(update.message().chat().id(), message);
    }
}
