package edu.java.bot.commands.impl;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.commands.Command;
import edu.java.bot.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class DeleteCommand implements Command {
    private static final String COMMAND_NAME = "/delete";
    private static final String COMMAND_DESCRIPTION = "Прекратить работу и удалить данные";
    private static final String SUCCESS_MESSAGE = "Чат успешно удален.";
    private static final String DEFAULT_ERROR_MESSAGE = "В работе чата произошла ошибка, повторите попытку позднее";
    private static final String CHAT_NOT_FOUND_MESSAGE = "Вы не зарегистрированы!";

    private final ScrapperClient scrapperClient;

    @Override
    public String execute(Update update) {
        var tgChatId = update.message().chat().id();

        try {
            scrapperClient.deleteChat(tgChatId);
            log.info("Команда delete выполнена");
            return SUCCESS_MESSAGE;
        } catch (BadRequestException e) {
            return switch (e.getApiErrorResponse().code()) {
                case "404 NOT_FOUND" -> CHAT_NOT_FOUND_MESSAGE;
                default -> DEFAULT_ERROR_MESSAGE;
            };
        } catch (Exception e) {
            return DEFAULT_ERROR_MESSAGE;
        }
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return COMMAND_DESCRIPTION;
    }
}
