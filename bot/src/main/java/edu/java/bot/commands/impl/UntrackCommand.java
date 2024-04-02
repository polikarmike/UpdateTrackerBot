package edu.java.bot.commands.impl;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.commands.Command;
import edu.java.bot.exception.BadRequestException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class UntrackCommand implements Command {
    private static final String COMMAND_NAME = "/untrack";
    private static final String COMMAND_DESCRIPTION = "Прекратить отслеживание ссылки";
    private static final String MISSING_URL_MESSAGE = "Пожалуйста, предоставьте URL для удаления.";
    private static final String SUCCESS_MESSAGE = "Ссылка успешно удалена: ";
    private static final String DEFAULT_ERROR_MESSAGE = "В работе чата произошла ошибка, повторите попытку позднее";
    private static final String CHAT_NOT_FOUND_MESSAGE = "Вы не зарегистрированы!";
    private static final String LINK_NOT_FOUND_MESSAGE = "Ссылка не найдена!";
    private static final String ALREADY_ADDED_LINK_MESSAGE = "Данная ссылка уже есть в списке!";
    private static final String BAD_LINK_MESSAGE =
        "Введенная ссылка некорректна. Пожалуйста, убедитесь, что вы ввели правильную ссылку.";
    private static final String MISSING_CHAT_EXCEPTION = "MissingChatException";
    private static final String LINK_NOT_FOUND_EXCEPTION = "LinkNotFoundException";
    private static final String NOT_FOUND_ERROR_CODE = "404 NOT_FOUND";
    private static final String CONFLICT_ERROR_CODE = "409 CONFLICT";
    private static final String BAD_REQUEST_ERROR_CODE = "400 BAD_REQUEST";

    private final ScrapperClient scrapperClient;

    @Override
    public String execute(Update update) {
        var tgChatId = update.message().chat().id();
        var messageText = update.message().text();

        try {
            String[] parts = messageText.split(" ", 2);
            if (parts.length < 2) {
                return MISSING_URL_MESSAGE;
            }

            String url = parts[1];
            URI uri = URI.create(url);

            scrapperClient.removeLink(tgChatId, uri);

            log.info("Команда untrack выполнена");
            return SUCCESS_MESSAGE + url;
        } catch (BadRequestException e) {
            return switch (e.getApiErrorResponse().code()) {
                case NOT_FOUND_ERROR_CODE -> switch (e.getApiErrorResponse().exceptionName()) {
                    case MISSING_CHAT_EXCEPTION -> CHAT_NOT_FOUND_MESSAGE;
                    case LINK_NOT_FOUND_EXCEPTION -> LINK_NOT_FOUND_MESSAGE;
                    default -> DEFAULT_ERROR_MESSAGE;
                };
                case CONFLICT_ERROR_CODE -> ALREADY_ADDED_LINK_MESSAGE;
                case BAD_REQUEST_ERROR_CODE -> BAD_LINK_MESSAGE;
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
