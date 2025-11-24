package ru.practicum.shareit.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.BadRequestException;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String email = (String) body.get("email");

        if (name == null || name.trim().isEmpty() || email == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Не указан email или имя"));

        if (!isValidEmail(email))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Невалидный email"));

        if (users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email)))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email уже занят"));

        Long id = idGen.getAndIncrement();
        User u = User.builder().id(id).name(name).email(email).build();
        users.put(id, u);

        return ResponseEntity.status(HttpStatus.CREATED).body(u);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        User u = users.get(id);
        if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        return ResponseEntity.ok(u);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = users.get(id);
        if (u == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));

        String name = (String) body.get("name");
        String email = (String) body.get("email");

        if (email != null) {
            if (!isValidEmail(email))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Невалидный email"));
            if (users.values().stream().anyMatch(us -> !us.getId().equals(id) && us.getEmail().equalsIgnoreCase(email)))
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email уже занят"));
            u.setEmail(email);
        }
        if (name != null) u.setName(name);

        users.put(id, u);
        return ResponseEntity.ok(u);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        users.remove(id);
        return ResponseEntity.ok().build();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && !email.contains(" ");
    }
}