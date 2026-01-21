package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.dto.item.CommentRequestDto;
import ru.practicum.shareit.dto.item.ItemCreateDto;
import ru.practicum.shareit.dto.item.ItemUpdateDto;

@Service
public class ItemClient {
    private final RestTemplate restTemplate;

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/items"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Object> createItem(ItemCreateDto dto, Long userId) {
        return restTemplate.postForEntity("?userId={userId}", dto, Object.class, userId);
    }

    public ResponseEntity<Object> updateItem(Long itemId, ItemUpdateDto dto, Long userId) {
        return restTemplate.patchForObject("/{itemId}?userId={userId}", dto, ResponseEntity.class, itemId, userId);
    }

    public ResponseEntity<Object> getItem(Long itemId, Long userId) {
        return restTemplate.getForEntity("/{itemId}?userId={userId}", Object.class, itemId, userId);
    }

    public ResponseEntity<Object> getItems(Long userId) {
        return restTemplate.getForEntity("?userId={userId}", Object.class, userId);
    }

    public ResponseEntity<Object> searchItems(String text) {
        return restTemplate.getForEntity("/search?text={text}", Object.class, text);
    }

    public ResponseEntity<Object> addComment(Long itemId, Long userId, CommentRequestDto commentRequestDto) {
        return restTemplate.postForEntity("/{itemId}/comment?userId={userId}", commentRequestDto, Object.class, itemId, userId);
    }
}