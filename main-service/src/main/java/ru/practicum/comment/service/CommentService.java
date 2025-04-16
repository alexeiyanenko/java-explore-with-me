package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {

    CommentResponseDto createComment(CommentRequestDto commentRequestDto, Long userId, Long eventId);

    CommentResponseDto updateComment(CommentRequestDto commentRequestDto, Long userId, Long commentId);

    CommentResponseDto getCommentById(Long commentId, Long userId);

    CommentResponseDto getCommentByIdAdmin(Long commentId);

    List<CommentResponseDto> getAllCommentsByUserId(Long userId, int from, int size);

    List<CommentResponseDto> getAllCommentsByEventId(Long eventId, int from, int size);

    void deleteComment(Long commentId);

    void deleteCommentByUserId(Long commentId, Long userId);

    List<CommentResponseDto> searchCommentsByText(String text, int from, int size);
}
