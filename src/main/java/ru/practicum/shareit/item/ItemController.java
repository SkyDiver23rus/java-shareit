package ru.practicum.shareit.item;

import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.util.HeaderConstants;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = HeaderConstants.SHARER_USER_ID, required = false) @NotNull(message = "Не указан обязательный заголовок 'X-Sharer-User-Id'") Long userId,
            @Valid @RequestBody ItemCreateDto dto) {


        var saved = itemService.addItem(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> update(
            @RequestHeader(value = HeaderConstants.SHARER_USER_ID, required = false) @NotNull(message = "Не указан обязательный заголовок 'X-Sharer-User-Id'") Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemUpdateDto dto) {

        var updated = itemService.updateItem(itemId, dto, userId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> get(@PathVariable Long itemId) {
        var item = itemService.getItem(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAll(@RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {
        return ResponseEntity.ok(itemService.getItemsOfUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam(value = "text", required = false) String text) {
        return ResponseEntity.ok(itemService.search(text));
    }
}