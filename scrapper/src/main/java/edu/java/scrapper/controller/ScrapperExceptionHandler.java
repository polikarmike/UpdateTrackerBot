package edu.java.scrapper.controller;

import edu.java.common.dto.responses.ApiErrorResponse;
import edu.java.scrapper.exception.BadRequestException;
import edu.java.scrapper.exception.InvalidLinkException;
import edu.java.scrapper.exception.LinkNotFoundException;
import edu.java.scrapper.exception.MissingChatException;
import edu.java.scrapper.exception.RepeatedLinkAdditionException;
import edu.java.scrapper.exception.RepeatedRegistrationException;
import edu.java.scrapper.exception.ServerException;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@RestControllerAdvice
public class ScrapperExceptionHandler {
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

    @ExceptionHandler(InvalidLinkException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleInvalidLinkException(InvalidLinkException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST.toString(), ex);
    }

    @ExceptionHandler(RepeatedRegistrationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleRepeatedRegistrationException(RepeatedRegistrationException ex) {
        return createErrorResponse(HttpStatus.CONFLICT.toString(), ex);
    }


    @ExceptionHandler(RepeatedLinkAdditionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleRepeatedLinkAdditionException(RepeatedLinkAdditionException ex) {
        return createErrorResponse(HttpStatus.CONFLICT.toString(), ex);
    }

    @ExceptionHandler(MissingChatException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleMssingChatException(MissingChatException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND.toString(), ex);
    }

    @ExceptionHandler(LinkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleLinkNotFoundException(LinkNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND.toString(), ex);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNoResourceFoundException(NoResourceFoundException ex) {
        return createErrorResponse(ex.getStatusCode().toString(), ex);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleNoHandlerFoundException(NoHandlerFoundException ex) {
        return createErrorResponse(ex.getStatusCode().toString(), ex);
    }

    @ExceptionHandler(ServerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleServerException(ServerException ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex);
    }


    private ApiErrorResponse createErrorResponse(String statusCode, Exception ex) {
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
            .map(StackTraceElement::toString)
            .toList();
        return new ApiErrorResponse(
            "Scrapper Error",
            statusCode,
            ex.getClass().getSimpleName(),
            ex.getMessage(),
            stackTraceList
        );
    }
}
