package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping("/{commentId}")
    public CommentResponseDto getCommentByIdAdmin(@PathVariable Long commentId) {
        log.info("Admin-запрос, на получение комментария с id {}", commentId);
        return commentService.getCommentByIdAdmin(commentId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Admin-запрос на удаление комментария с id {}", commentId);
        commentService.deleteComment(commentId);
    }

    @GetMapping("/search")
    public List<CommentResponseDto> searchComments(@RequestParam String text,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на поиск комментариев по тексту: {}", text);
        return commentService.searchCommentsByText(text, from, size);
    }
}
