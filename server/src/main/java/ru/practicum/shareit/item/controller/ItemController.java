package ru.practicum.shareit.item.controller;

import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.util.HeaderConstants;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> create(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @Valid @RequestBody ItemCreateDto dto) {

        ItemDto saved = itemService.addItem(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto dto) {

        ItemDto updated = itemService.updateItem(itemId, dto, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemWithBookingsDto> get(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long itemId) {
        ItemWithBookingsDto item = itemService.getItem(itemId, userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemWithBookingsDto>> getAll(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {
        List<ItemWithBookingsDto> items = itemService.getItemsOfUser(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(
            @RequestParam(value = "text", required = false) String text) {
        List<ItemDto> items = itemService.search(text);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentResponseDto> addComment(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto requestDto) {
        log.info("POST /items/{}/comment with userId={}", itemId, userId);
        CommentResponseDto comment = itemService.addComment(userId, itemId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
}