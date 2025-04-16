package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.event.model.Event;

import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Set<Event> findAllByIdIn(Set<Long> ids);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);
}
