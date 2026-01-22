package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.dto.user.UserDto;
import java.util.function.Supplier;
import org.springframework.http.client.ClientHttpRequestFactory;

@Service
public class UserClient {
    private final RestTemplate restTemplate;

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/users"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Object> createUser(UserDto dto) {
        return restTemplate.postForEntity("", dto, Object.class);
    }

    public ResponseEntity<Object> updateUser(Long userId, UserDto dto) {
        return restTemplate.patchForObject("/{userId}", dto, ResponseEntity.class, userId);
    }

    public ResponseEntity<Object> getUser(Long userId) {
        return restTemplate.getForEntity("/{userId}", Object.class, userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return restTemplate.getForEntity("", Object.class);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        restTemplate.delete("/{userId}", userId);
        return ResponseEntity.ok().build();
    }
}
