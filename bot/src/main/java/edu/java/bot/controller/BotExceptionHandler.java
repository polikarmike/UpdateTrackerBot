package edu.java.bot.controller;

import edu.java.bot.exception.BadRequestException;
import edu.java.common.dto.responses.ApiErrorResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Component
@RestControllerAdvice
public class BotExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        return createErrorResponse(ex.getStatusCode().toString(), ex);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidationExceptions(BadRequestException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST.toString(), ex);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return createErrorResponse(ex.getStatusCode().toString(), ex);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleException(Exception ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex);
    }

    private ApiErrorResponse createErrorResponse(String statusCode, Exception ex) {
        String errorMessage = ex.getMessage();
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();
        return new ApiErrorResponse(
            "Bot Error",
            statusCode,
            ex.getClass().getSimpleName(),
            errorMessage,
            stackTraceList
        );
    }
}
