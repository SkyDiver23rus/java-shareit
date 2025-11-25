package ru.practicum.shareit.item;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.HeaderConstants;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> add(
            @RequestHeader(value = HeaderConstants.SHARER_USER_ID, required = false) Long userId,
            @RequestBody ItemDto dto) {
        try {
            ItemDto saved = service.addItem(dto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            if ("User not found".equals(ex.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> update(
            @RequestHeader(value = HeaderConstants.SHARER_USER_ID, required = false) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto dto) {
        try {
            ItemDto upd = service.updateItem(itemId, dto, userId);
            return ResponseEntity.ok(upd);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Item not found"));
        } catch (IllegalArgumentException e) {
            if ("Access denied".equals(e.getMessage()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> get(@PathVariable Long itemId) {
        ItemDto dto = service.getItem(itemId);
        if (dto == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Item not found"));
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAll(@RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId) {
        return ResponseEntity.ok(service.getItemsOfUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(
            @RequestParam(value = "text") String text) {
        return ResponseEntity.ok(service.search(text));
    }
}