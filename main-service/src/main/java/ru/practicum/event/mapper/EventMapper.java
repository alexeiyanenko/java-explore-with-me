package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, LocationMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(source = "views", target = "views")
    EventFullDto toFullDto(Event event);

    @Mapping(source = "views", target = "views")
    EventShortDto toShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "views", ignore = true)
    Event toEventFromNewEvent(NewEventDto newEventDto);

}
