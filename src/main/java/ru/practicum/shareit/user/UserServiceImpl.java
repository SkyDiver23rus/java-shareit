package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;

import java.util.List;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto addUser(UserDto dto) {
        if (dto.getEmail() == null || !dto.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email duplicate");
        }
        User user = UserMapper.toUser(dto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id).orElseThrow();
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (!dto.getEmail().contains("@")) throw new IllegalArgumentException("Invalid email");
            if (userRepository.findByEmail(dto.getEmail()).isPresent())
                throw new IllegalStateException("Email duplicate");
            user.setEmail(dto.getEmail());
        }
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findById(id).map(UserMapper::toUserDto).orElse(null);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        return userRepository.findById(id).isPresent();
    }
}