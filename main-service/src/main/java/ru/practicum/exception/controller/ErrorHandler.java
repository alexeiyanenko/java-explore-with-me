package ru.practicum.exception.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.*;
import ru.practicum.exception.model.ApiError;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("409 {}", e.getMessage(), e);
        return ApiError.builder()
                .message("Ошибка: нарушение целостности данных")
                .reason("Нарушение уникальности в базе данных")
                .status(HttpStatus.CONFLICT.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("400 {}", e.getMessage(), e);
        return ApiError.builder()
                .message("Ошибка валидации параметров запроса")
                .reason("Некорректный запрос")
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
                .reason("Некорректный запрос")
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.warn("404 {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Запрашиваемый объект не найден")
                .status(HttpStatus.NOT_FOUND.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.warn("409 {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Нарушение целостности данных")
                .status(HttpStatus.CONFLICT.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDeniedException(AccessDeniedException e) {
        log.warn("403 {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Доступ к запрашиваемому ресурсу запрещён")
                .status(HttpStatus.FORBIDDEN.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDuplicateRequestException(DuplicateRequestException e) {
        log.warn("409 {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Повторный запрос невозможен")
                .status(HttpStatus.CONFLICT.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInvalidEventStateException(InvalidEventStateException e) {
        log.warn("409 {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Состояние события недопустимо для выполнения операции")
                .status(HttpStatus.CONFLICT.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("400 {}", e.getMessage());
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Неверные параметры запроса")
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("400 {}", errorMessages);

        return ApiError.builder()
                .message("Ошибка валидации параметров запроса")
                .reason("Некорректный запрос")
                .status(HttpStatus.BAD_REQUEST.name())
                .errors(errorMessages)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAllOtherExceptions(Throwable e) {
        log.error("500 {}", e.getMessage(), e);
        return ApiError.builder()
                .message("Внутренняя ошибка сервера")
                .reason("Произошла непредвиденная ошибка")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .errors(Collections.singletonList(e.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
