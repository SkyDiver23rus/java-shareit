package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.UserCreateDto;
import ru.practicum.shareit.dto.UserUpdateDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.map.UserMapper;
import ru.practicum.shareit.model.User;
import ru.practicum.shareit.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto dto) {
        repository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email уже существует");
        });
        User user = UserMapper.toUser(dto);
        User saved = repository.save(user);
        return UserMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            repository.findByEmail(dto.getEmail()).ifPresent(u -> {
                throw new IllegalStateException("Email уже существует");
            });
        }

        UserMapper.updateUserFromDto(dto, existing);
        return UserMapper.toUserDto(repository.save(existing));
    }

    @Override
    public UserDto getUserById(Long id) {
        return repository.findById(id)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        return repository.existsById(id);
    }
}