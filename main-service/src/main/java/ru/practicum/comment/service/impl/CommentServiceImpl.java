package ru.practicum.comment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentRequestDto;
import ru.practicum.comment.dto.CommentResponseDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.comment.service.CommentService;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.AccessDeniedException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;


    @Override
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto, Long userId, Long eventId) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие еще не опубликовано");
        }
        Comment comment = commentMapper.toComment(commentRequestDto, user, event);
        comment.setCreated(LocalDateTime.now());
        CommentResponseDto saveComment = commentMapper.toCommentResponseDto(commentRepository.save(comment));
        log.info("Комментарий с id {} успешно создан", saveComment.getId());
        return saveComment;
    }

    @Override
    public CommentResponseDto updateComment(CommentRequestDto commentRequestDto, Long userId, Long commentId) {
        checkUser(userId);
        Comment oldComment = checkComment(commentId);
        if (!oldComment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь с id " + userId + " не является автором комментария");
        }
        oldComment.setText(commentRequestDto.getText());
        CommentResponseDto updateComment = commentMapper.toCommentResponseDto(commentRepository.save(oldComment));
        log.info("Комментарий с id {} успешно обновлен", updateComment.getId());
        return updateComment;
    }

    @Override
    public CommentResponseDto getCommentById(Long commentId, Long userId) {
        Comment comment = checkComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь с id " + userId + " не является автором комментария");
        }
        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(comment);
        return commentResponseDto;
    }

    @Override
    public CommentResponseDto getCommentByIdAdmin(Long commentId) {
        Comment comment = checkComment(commentId);
        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(comment);
        return commentResponseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllCommentsByUserId(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByAuthor_IdOrderByCreatedDesc(userId, pageable);
        return comments.stream()
                .map(commentMapper::toCommentResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getAllCommentsByEventId(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByEvent_IdOrderByCreatedDesc(eventId, pageable);
        return comments.stream()
                .map(commentMapper::toCommentResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> searchCommentsByText(String text, int from, int size) {
        if (text.isBlank()) {
            throw new ValidationException("Текст поискового запроса не должен быть пустым");
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByTextContainingIgnoreCase(text, pageable);
        return comments.stream()
                .map(commentMapper::toCommentResponseDto)
                .toList();
    }


    @Override
    public void deleteComment(Long commentId) {
        checkComment(commentId);
        commentRepository.deleteById(commentId);
        log.info("Комментарий успешно удален администратором");
    }

    @Override
    public void deleteCommentByUserId(Long commentId, Long userId) {
        Comment comment = checkComment(commentId);
        checkUser(userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("Пользователь с id " + userId + " не является автором комментария");
        }
        commentRepository.deleteById(commentId);
        log.info("Комментарий успешно удален автором");
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("user с id " + userId + " не найден"));
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event с id " + eventId + " не найден"));
    }

    private Comment checkComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Comment с id " + commentId + " не найден"));
    }
}
