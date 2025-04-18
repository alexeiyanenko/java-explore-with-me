package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createComment(@Valid @RequestBody CommentRequestDto commentRequestDto,
                                            @PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на создание комментария");
        return commentService.createComment(commentRequestDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentResponseDto updateComment(@Valid @RequestBody CommentRequestDto commentRequestDto,
                                            @PathVariable Long userId, @PathVariable Long commentId) {
        log.info("Запрос на обновление комментария с id {}", commentId);
        return commentService.updateComment(commentRequestDto, userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentResponseDto getCommentById(@PathVariable Long commentId,
                                             @PathVariable Long userId) {
        log.info("Запрос на получение комментария с id {}", commentId);
        return commentService.getCommentById(commentId, userId);
    }

    @GetMapping
    public List<CommentResponseDto> getAllCommentsByUserId(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "0") int from,
                                                           @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение всех комментариев пользователя с id {}", userId);
        return commentService.getAllCommentsByUserId(userId, from, size);
    }

    @DeleteMapping("/{commentId}")
    public void deleteCommentByUserId(@PathVariable Long commentId, @PathVariable Long userId) {
        log.info("Запрос на удаление комментария с id {} от пользователя с id {}", commentId, userId);
        commentService.deleteCommentByUserId(commentId, userId);
    }

}
