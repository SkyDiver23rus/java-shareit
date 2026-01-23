package ru.practicum.shareit.request.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.util.HeaderConstants;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {

    private final ItemRequestService requestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> create(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @Valid @RequestBody ItemRequestCreateDto createDto) {
        log.info("POST /requests with userId={}", userId);
        ItemRequestDto created = requestService.create(userId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getOwnRequests(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {
        log.info("GET /requests with userId={}", userId);
        List<ItemRequestDto> requests = requestService.getOwnRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET /requests/all with userId={}, from={}, size={}", userId, from, size);
        List<ItemRequestDto> requests = requestService.getAllRequests(userId, from, size);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getById(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long requestId) {
        log.info("GET /requests/{} with userId={}", requestId, userId);
        ItemRequestDto request = requestService.getRequestById(userId, requestId);
        return ResponseEntity.ok(request);
    }
}