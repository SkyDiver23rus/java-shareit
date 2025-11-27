package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.*;
import ru.practicum.shareit.user.model.User;


public class UserMapper {
    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto dto) {
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static User fromCreateDto(UserCreateDto dto) {
        if (dto == null) return null;
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .build();
    }

    public static User fromUpdateDto(UserUpdateDto dto, User existing) {
        if (existing == null) return null;
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        return existing;
    }
}