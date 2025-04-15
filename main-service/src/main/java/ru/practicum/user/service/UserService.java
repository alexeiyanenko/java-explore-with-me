package ru.practicum.user.service;

import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserRequestDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserRequestDto userRequestDto);

    void delete(Long userId);

    List<UserDto> getUsers(List<Long> ids, int from, int size);
}
