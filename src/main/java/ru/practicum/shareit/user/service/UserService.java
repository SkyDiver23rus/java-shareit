package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(UserCreateDto dto);

    UserDto updateUser(Long id, UserUpdateDto dto);

    UserDto getUserById(Long id);

    List<UserDto> getAllUsers();

    void deleteUser(Long id);

    boolean exists(Long id);
}