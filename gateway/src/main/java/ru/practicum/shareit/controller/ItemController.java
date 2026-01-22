package ru.practicum.shareit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.client.ItemClient;
import ru.practicum.shareit.dto.item.CommentRequestDto;
import ru.practicum.shareit.dto.item.ItemCreateDto;
import ru.practicum.shareit.dto.item.ItemUpdateDto;
import ru.practicum.shareit.util.HeaderConstants;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @Valid @RequestBody ItemCreateDto dto) {

        return itemClient.createItem(dto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto dto) {

        return itemClient.updateItem(itemId, dto, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long itemId) {

        return itemClient.getItem(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {

        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam(value = "text", required = false) String text) {

        return itemClient.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {

        return itemClient.addComment(itemId, userId, commentRequestDto);
    }
}
