package ru.practicum.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Throwable e) {
        log.error("500 {}", e.getMessage(), e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Ошибка сервера")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("400 {}", e.getMessage(), e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Некорректные параметры запроса")
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException e) {
        log.warn("400 {}", e.getMessage(), e);
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Ошибка валидации данных запроса")
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
