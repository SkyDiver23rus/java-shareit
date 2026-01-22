package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.RequestClient;
import ru.practicum.shareit.dto.request.ItemRequestDto;
import ru.practicum.shareit.util.HeaderConstants;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class RequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> add(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @Valid @RequestBody ItemRequestDto dto) {

        return requestClient.createRequest(dto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {

        return requestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {

        return requestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long requestId) {

        return requestClient.getRequestById(requestId, userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long id) {

        return requestClient.deleteRequest(id, userId);
    }
}
