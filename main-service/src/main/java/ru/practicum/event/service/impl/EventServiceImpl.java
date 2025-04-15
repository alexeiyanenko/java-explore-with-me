package ru.practicum.event.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatClient;
import ru.practicum.ViewStats;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.enums.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.event.specification.EventSpecifications;
import ru.practicum.exception.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.enums.RequestState;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final StatClient statClient;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(Long userId, int from, int size) {
        checkUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        Page<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        List<Event> eventList = events.getContent();
        setViews(eventList);
        return eventList.stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    public EventFullDto createEvent(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.toEventFromNewEvent(newEventDto);
        User initiator = checkUser(userId);
        Category category = checkCategory(newEventDto.getCategory());
        Location location = locationRepository.save(locationMapper.toLocation(newEventDto.getLocation()));
        event.setInitiator(initiator);
        event.setLocation(location);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setConfirmedRequests(0);
        event.setState(EventState.PENDING);
        Event saveEvent = eventRepository.save(event);
        log.info("Событие успешно добавлено");
        return eventMapper.toFullDto(saveEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long userId, Long eventId) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new AccessDeniedException("Ошибка доступа: пользователь не является инициатором события");
        }
        setViews(List.of(event));

        return eventMapper.toFullDto(event);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest userRequest) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new AccessDeniedException("Ошибка доступа: пользователь не является инициатором события");
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new InvalidEventStateException("Событие уже опубликовано");
        }
        if (userRequest.getEventDate() != null) {
            if (userRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException("Дата события должна быть не раньше чем через 2 часа от текущего момента");
            }
            event.setEventDate(userRequest.getEventDate());
        }
        if (userRequest.getAnnotation() != null) {
            event.setAnnotation(userRequest.getAnnotation());
        }
        if (userRequest.getCategory() != null) {
            Category category = checkCategory(userRequest.getCategory());
            event.setCategory(category);
        }
        if (userRequest.getDescription() != null) {
            event.setDescription(userRequest.getDescription());
        }
        if (userRequest.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(userRequest.getLocation()));
            event.setLocation(location);
        }
        if (userRequest.getPaid() != null) {
            event.setPaid(userRequest.getPaid());
        }
        if (userRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(userRequest.getParticipantLimit());
        }
        if (userRequest.getRequestModeration() != null) {
            event.setRequestModeration(userRequest.getRequestModeration());
        }
        if (userRequest.getTitle() != null) {
            event.setTitle(userRequest.getTitle());
        }
        if (userRequest.getStateAction() != null) {
            switch (userRequest.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                default -> throw new InvalidEventStateException("Некорректное действие состояния");
            }
        }
        log.info("Успешное обновление события пользователем");
        Event saveEvent = eventRepository.save(event);
        return eventMapper.toFullDto(saveEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new AccessDeniedException("Ошибка доступа: пользователь не является инициатором события");
        }
        List<Request> requests = requestRepository.findAllByEventId(eventId);

        return requests.stream()
                .map(requestMapper::toRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest dto) {
        User user = checkUser(userId);
        Event event = checkEvent(eventId);
        if (!event.getInitiator().getId().equals(user.getId())) {
            throw new AccessDeniedException("Ошибка доступа: пользователь не является инициатором события");
        }
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("Подтверждение заявок не требуется для данного события");
        }
        List<Request> requests = requestRepository.findAllById(dto.getRequestIds());

        long available = event.getParticipantLimit() - event.getConfirmedRequests();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : requests) {
            if (!request.getStatus().equals(RequestState.PENDING)) {
                throw new ConflictException("Можно обрабатывать только заявки в статусе PENDING");
            }

            if (dto.getStatus() == RequestState.CONFIRMED) {
                if (available <= 0) {
                    throw new ConflictException("Лимит участников достигнут. Подтверждение невозможно.");
                }

                request.setStatus(RequestState.CONFIRMED);
                available--;
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                confirmed.add(requestMapper.toRequestDto(request));
            } else if (dto.getStatus() == RequestState.REJECTED) {
                request.setStatus(RequestState.REJECTED);
                rejected.add(requestMapper.toRequestDto(request));
            }
        }
        log.info("успешное обновления статуса события");

        requestRepository.saveAll(requests);
        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsAdmin(List<Long> userIds, List<String> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("rangeStart не может быть позже rangeEnd");
        }

        Specification<Event> spec = Specification.where(null);

        if (userIds != null && !userIds.isEmpty()) {
            spec = spec.and(EventSpecifications.initiators(userIds));
        }

        if (states != null && !states.isEmpty()) {
            spec = spec.and(EventSpecifications.statesIn(convertStates(states)));
        }

        if (categories != null && !categories.isEmpty()) {
            spec = spec.and(EventSpecifications.inCategories(categories));
        }

        if (rangeStart != null) {
            spec = spec.and(EventSpecifications.rangeStart(rangeStart));
        }

        if (rangeEnd != null) {
            spec = spec.and(EventSpecifications.rangeEnd(rangeEnd));
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();

        setViews(events);

        return events.stream()
                .map(eventMapper::toFullDto)
                .toList();
    }

    @Override
    public EventFullDto updateEventFromAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = checkEvent(eventId);
        if (event.getState() != EventState.PENDING) {
            throw new InvalidEventStateException("Можно редактировать только события в состоянии ожидания");
        }
        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ValidationException("Дата начала события должна быть не ранее чем через час");
            }
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getCategory() != null) {
            Category category = checkCategory(updateRequest.getCategory());
            event.setCategory(category);
        }
        if (updateRequest.getLocation() != null) {
            Location location = locationRepository.save(locationMapper.toLocation(updateRequest.getLocation()));
            event.setLocation(location);
        }

        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> event.setState(EventState.CANCELED);
                default -> throw new ValidationException("Неизвестное действие");
            }
        }
        log.info("Успешное обновление от Admin");
        Event saved = eventRepository.save(event);
        return eventMapper.toFullDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, int from, int size, HttpServletRequest httpServletRequest) {

        String uri = "/events";
        String ip = httpServletRequest.getRemoteAddr();
        statClient.createHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("rangeStart не может быть позже rangeEnd");
        }

        Specification<Event> spec = Specification.where(EventSpecifications.isPublished())
                .and(EventSpecifications.textInAnnotationOrDescription(text))
                .and(EventSpecifications.inCategories(categories))
                .and(EventSpecifications.isPaid(paid))
                .and(EventSpecifications.rangeStart(rangeStart))
                .and(EventSpecifications.rangeEnd(rangeEnd))
                .and(onlyAvailable != null && onlyAvailable ? EventSpecifications.onlyAvailable() : null);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = new ArrayList<>(eventRepository.findAll(spec, pageable).getContent());

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        setViews(events);

        if (sort != null) {
            switch (sort.toUpperCase()) {
                case "VIEWS" ->
                        events.sort(Comparator.comparing(Event::getViews, Comparator.nullsFirst(Long::compareTo)).reversed());
                case "EVENT_DATE" -> events.sort(Comparator.comparing(Event::getEventDate));
                default -> throw new ValidationException("sort должен быть либо VIEWS, либо EVENT_DATE");
            }
        }
        return events.stream()
                .map(eventMapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByIdPublic(Long eventId, HttpServletRequest httpServletRequest) {
        Event event = checkEvent(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Событие не опубликовано");
        }

        String uri = "/events/" + eventId;
        String ip = httpServletRequest.getRemoteAddr();
        statClient.createHit(EndpointHitDto.builder()
                .app("main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        setViews(event);
        return eventMapper.toFullDto(event);
    }

    private void setViews(Event event) {
        List<ViewStats> stats = statClient.getStats(
                event.getPublishedOn(),
                LocalDateTime.now().plusMinutes(1),
                List.of("/events/" + event.getId()),
                true
        );
        long views = stats.isEmpty() ? 0 : stats.getFirst().getHits();
        event.setViews(views);
    }

    private void setViews(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<String> uris = new ArrayList<>();
        LocalDateTime start = null;
        for (Event event : events) {
            if (event.getPublishedOn() != null) {
                uris.add("/events/" + event.getId());
                if (start == null || event.getPublishedOn().isBefore(start)) {
                    start = event.getPublishedOn();
                }
            }
        }

        if (start == null) {
            return;
        }

        List<ViewStats> stats = statClient.getStats(
                start,
                LocalDateTime.now().plusMinutes(1),
                uris,
                true
        );

        if (stats.isEmpty() || stats == null) {
            return;
        }

        Map<Long, Long> viewsById = stats.stream()
                .collect(Collectors.toMap(
                        viewStats -> Long.parseLong(viewStats.getUri().substring(viewStats.getUri().lastIndexOf("/") + 1)),
                        ViewStats::getHits
                ));

        for (Event event : events) {
            event.setViews(viewsById.getOrDefault(event.getId(), 0L));
        }
    }

    private User checkUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("user с id " + userId + " не найден"));
    }

    private Category checkCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Category с id " + categoryId + " не найден"));
    }

    private Event checkEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Event с id " + eventId + " не найден"));
    }

    private List<EventState> convertStates(List<String> states) {
        return states.stream()
                .map(s -> {
                    try {
                        return EventState.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new ValidationException("Unknown state: " + s);
                    }
                })
                .toList();
    }

}
