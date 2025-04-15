package ru.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStats;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;

    @Override
    public void createHit(EndpointHitDto dto) {
        statRepository.save(EndpointHitMapper.toEndpointHit(dto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Некорректный диапазон времени: start позже end");
        }

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statRepository.findStatsUnique(start, end);
            } else {
                return statRepository.findStats(start, end);
            }
        } else {
            if (unique) {
                return statRepository.findStatsUniqueWithUris(start, end, uris);
            } else {
                return statRepository.findStatsWithUris(start, end, uris);
            }
        }
    }
}