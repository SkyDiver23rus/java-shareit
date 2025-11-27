package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.*;
import ru.practicum.shareit.user.model.User;


import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    public UserServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDto createUser(UserCreateDto dto) {
        repository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email уже существует");
        });
        User user = UserMapper.fromCreateDto(dto);
        User saved = repository.save(user);
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User existing = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            repository.findByEmail(dto.getEmail()).ifPresent(u -> {
                throw new IllegalStateException("Email уже существует");
            });
        }
        User updated = UserMapper.fromUpdateDto(dto, existing);
        return UserMapper.toUserDto(repository.save(updated));
    }

    @Override
    public UserDto getUserById(Long id) {
        return repository.findById(id).map(UserMapper::toUserDto).orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream().map(UserMapper::toUserDto).toList();
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        return repository.findById(id).isPresent();
    }
}