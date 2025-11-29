package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.*;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    void deleteById(Long id);
}