package ru.practicum.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserRequestDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserRequestDto userRequestDto) {
        User user = userMapper.toUser(userRequestDto);
        UserDto userDto = userMapper.toUserDto(userRepository.save(user));
        log.info("User с Id {} успешно создан", userDto.getId());
        return userDto;
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
        log.info("User с Id {} успешно удален", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findUsersByIdIn(ids, pageable);
        }
        return users.getContent().stream().map(userMapper::toUserDto).toList();
    }
}
