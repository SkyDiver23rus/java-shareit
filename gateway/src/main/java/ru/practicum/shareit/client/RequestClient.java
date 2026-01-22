package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.dto.request.ItemRequestDto;
import java.util.function.Supplier;
import org.springframework.http.client.ClientHttpRequestFactory;

@Service
public class RequestClient {
    private final RestTemplate restTemplate;

    @Autowired
    public RequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/requests"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Object> createRequest(ItemRequestDto dto, Long userId) {
        return restTemplate.postForEntity("?userId={userId}", dto, Object.class, userId);
    }

    public ResponseEntity<Object> getOwnRequests(Long userId) {
        return restTemplate.getForEntity("?userId={userId}", Object.class, userId);
    }

    public ResponseEntity<Object> getAllRequests(Long userId) {
        return restTemplate.getForEntity("/all?userId={userId}", Object.class, userId);
    }

    public ResponseEntity<Object> getRequestById(Long requestId, Long userId) {
        return restTemplate.getForEntity("/{requestId}?userId={userId}", Object.class, requestId, userId);
    }

    public ResponseEntity<Object> deleteRequest(Long id, Long userId) {
        restTemplate.delete("/{id}?userId={userId}", id, userId);
        return ResponseEntity.ok().build();
    }
}
