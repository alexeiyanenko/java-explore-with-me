package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/view/{commentId}")
    public CommentResponseDto getCommentById(@PathVariable Long commentId) {
        log.info("Запрос на получение комментария с id {}", commentId);
        return commentService.getCommentByIdAdmin(commentId);
    }

    @GetMapping("/{eventId}")
    public List<CommentResponseDto> getAllCommentsByEventId(@PathVariable Long eventId,
                                                            @RequestParam(defaultValue = "0") int from,
                                                            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение всех комментариев события с id {}", eventId);
        return commentService.getAllCommentsByEventId(eventId, from, size);
    }
}
