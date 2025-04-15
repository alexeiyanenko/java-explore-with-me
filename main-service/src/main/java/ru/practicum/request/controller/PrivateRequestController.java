package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Запрос на участие в событие");
        return requestService.createRequest(userId, eventId);
    }

    @GetMapping
    List<ParticipationRequestDto> getAllRequests(@PathVariable Long userId) {
        log.info("Запрос на получение информации о заявках текущего пользователя на участие в чужих событиях");
        return requestService.getAllRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Запрос на отмену своего запроса на участие в событии");
        return requestService.cancelRequest(userId, requestId);
    }
}
