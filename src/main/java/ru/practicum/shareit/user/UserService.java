package ru.practicum.shareit.user;

import java.util.List;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    void deleteUser(Long userId);

    boolean exists(Long userId);
}