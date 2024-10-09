package ru.skillbox.auth_service.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import ru.skillbox.auth_service.exception.exceptions.AlreadyExistsException;
import ru.skillbox.auth_service.exception.exceptions.BadRequestException;
import ru.skillbox.auth_service.exception.exceptions.ObjectNotFoundException;
import ru.skillbox.auth_service.exception.exceptions.UnsupportedStateException;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlerNotFoundException(final ObjectNotFoundException e) {

        log.warn("404 {}", e.getMessage(), e);
        return new ErrorResponse("Object not found 404", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlerBadRequest(final BadRequestException e) {

        log.warn("400 {}", e.getMessage(), e);
        return new ErrorResponse("Неверный запрос ", e.getMessage());
    }

    @ExceptionHandler(UnsupportedStateException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handlerUnsupportedState(final UnsupportedStateException exception) {

        log.warn("403 {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage(), exception.getMessage());
    }

    @ExceptionHandler(UnsupportedStateException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handlerAlreadyExistsHandler(final AlreadyExistsException exception) {

        log.warn("403 {}", exception.getMessage(), exception);
        return new ErrorResponse("User already exists! "
                + exception.getMessage(), exception.getMessage());
    }

    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handlerInternalServerError(final HttpServerErrorException.InternalServerError exception) {

        log.warn("500 Что-то пошло не так {}", exception.getMessage(), exception);
        return new ErrorResponse("Что-то пошло не так ", exception.getMessage());
    }
}

