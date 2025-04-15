package ru.practicum.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.DuplicateRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.enums.RequestState;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.service.RequestService;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        checkRequest(userId, eventId);
        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictException("Инициатор не может подать запрос на участие");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие должно быть опубликовано");
        }

        if (event.getParticipantLimit() > 0 && event.getConfirmedRequests() >= event.getParticipantLimit()) {
            throw new ConflictException("Закончились места на мероприятия");
        }
        RequestState state;
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            state = RequestState.CONFIRMED;
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        } else {
            state = RequestState.PENDING;
        }

        Request request = Request.builder()
                .event(event)
                .requester(user)
                .status(state)
                .created(LocalDateTime.now())
                .build();
        Request saveRequest = requestRepository.save(request);
        if (request.getStatus() == RequestState.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        log.info("Запрос на участие в событие, успешно создан");
        return requestMapper.toRequestDto(saveRequest);
    }

    @Override
    public List<ParticipationRequestDto> getAllRequests(Long userId) {
        checkUser(userId);
        List<Request> requests = requestRepository.findAllByRequesterId(userId);
        return requests.stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUser(userId);
        Request request = checkRequestExist(requestId);
        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Пользователь не может отменить чужой запрос");
        }
        request.setStatus(RequestState.CANCELED);
        Request saveRequest = requestRepository.save(request);
        log.info("Запрос на участие в событие, успешно отменен");
        return requestMapper.toRequestDto(saveRequest);
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("user с id " + userId + " не найден"));
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event с id " + eventId + " не найден"));
    }

    private void checkRequest(Long userId, Long eventId) {
        requestRepository.findByRequesterIdAndEventId(userId, eventId)
                .ifPresent(request -> {
                    throw new DuplicateRequestException("Запрос уже создан");
                });
    }

    private Request checkRequestExist(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request с id " + requestId + " не найден"));
    }
}
