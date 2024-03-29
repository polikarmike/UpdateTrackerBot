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
public class StartCommand implements Command {
    private static final String COMMAND_NAME = "/start";
    private static final String COMMAND_DESCRIPTION = "Начать работу с ботом и зарегистрироваться";
    private static final String REGISTRATION_SUCCESS_MESSAGE = "Чат успешно зарегистрирован.";
    private static final String DEFAULT_ERROR_MESSAGE = "В работе чата произошла ошибка, повторите попытку позднее";
    private static final String ALREADY_REGISTERED_MESSAGE = "Вы уже зарегистрированы!";

    private final ScrapperClient scrapperClient;

    @Override
    public String execute(Update update) {
        var tgChatId = update.message().chat().id();

        try {
            scrapperClient.registerChat(tgChatId);
            log.info("Команда start выполнена");
            return REGISTRATION_SUCCESS_MESSAGE;
        } catch (BadRequestException e) {
            return switch (e.getApiErrorResponse().code()) {
                case "409 CONFLICT" -> ALREADY_REGISTERED_MESSAGE;
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
