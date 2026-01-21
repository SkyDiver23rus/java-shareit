package ru.practicum.shareit.server.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.service.ItemRequestService;
import ru.practicum.shareit.server.util.HeaderConstants;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService service;

    @PostMapping
    public ResponseEntity<ItemRequestDto> add(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @RequestBody ItemRequestDto dto) {
        ItemRequestDto saved = service.addRequest(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemRequestDto> get(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long id) {
        ItemRequestDto dto = service.getRequest(id, userId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getAll(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {
        List<ItemRequestDto> requests = service.getAll(userId);
        return ResponseEntity.ok(requests);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long id) {
        service.deleteRequest(id, userId);
        return ResponseEntity.ok().build();
    }
}