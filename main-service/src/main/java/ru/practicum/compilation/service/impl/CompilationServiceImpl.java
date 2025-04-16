package ru.practicum.compilation.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toCompilationFromNewCompilation(newCompilationDto);
        Set<Long> eventsId = newCompilationDto.getEvents();
        if (!eventsId.isEmpty()) {
            compilation.setEvents(eventRepository.findAllByIdIn(eventsId));
        }
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilationRepository.save(compilation));
        if (compilationDto.getEvents() == null) {
            compilationDto.setEvents(new HashSet<>());
        }
        log.info("Compilation с id {} успешно создан", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        Compilation compilation = checkCompilation(compId);
        Set<Long> eventsId = updateCompilationRequest.getEvents();
        if (!eventsId.isEmpty()) {
            compilation.setEvents(eventRepository.findAllByIdIn(eventsId));
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }
        CompilationDto compilationDto = compilationMapper.toCompilationDto(compilationRepository.save(compilation));
        log.info("Compilation с id {} успешно обновлен", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public void delete(Long compId) {
        checkCompilation(compId);
        compilationRepository.deleteById(compId);
        log.info("Compilation с id {} успешно удалена", compId);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        return compilationMapper.toCompilationDto(checkCompilation(compId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, pageable).stream()
                    .map(compilationMapper::toCompilationDto).toList();
        } else {
            return compilationRepository.findAll(pageable).stream()
                    .map(compilationMapper::toCompilationDto).toList();
        }
    }

    private Compilation checkCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(() ->
                new NotFoundException("Compilation с id " + compId + " не найдена"));
    }
}
