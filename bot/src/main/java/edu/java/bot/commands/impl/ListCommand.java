package edu.java.bot.commands.impl;

import com.pengrad.telegrambot.model.Update;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.commands.Command;
import edu.java.bot.exception.BadRequestException;
import edu.java.common.dto.responses.LinkResponse;
import edu.java.common.dto.responses.ListLinksResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class ListCommand implements Command {
    private static final String COMMAND_NAME = "/list";
    private static final String COMMAND_DESCRIPTION = "Отобразить список отслеживаемых ссылок";
    private static final String COMMAND_RESPONSE = "Здесь будет реализация команды /list";
    private static final String EMPTY_LIST_MESSAGE = "Список отслеживаемых ссылок пуст.";
    private static final String ERROR_MESSAGE = "Ошибка при получении списка ссылок: ";
    private static final String TRACKED_LINKS_HEADER = "Вот отслеживаемые ссылки:\n\n";
    private static final String DEFAULT_ERROR_MESSAGE = "В работе чата произошла ошибка, повторите попытку позднее";
    private static final String CHAT_NOT_FOUND_MESSAGE = "Вы не зарегистрированы!";

    private final ScrapperClient scrapperClient;

    @Override
    public String execute(Update update) {
        var tgChatId = update.message().chat().id();
        try {
            ListLinksResponse listLinksResponse = scrapperClient.getAllLinks(tgChatId);
            List<LinkResponse> links = listLinksResponse.links();

            if (links.isEmpty()) {
                return EMPTY_LIST_MESSAGE;
            }

            StringBuilder response = new StringBuilder();
            response.append(TRACKED_LINKS_HEADER);
            for (var link : links) {
                response.append("- ").append(link.url()).append("\n");
            }

            log.info("Команда list выполнена");
            return response.toString();
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
