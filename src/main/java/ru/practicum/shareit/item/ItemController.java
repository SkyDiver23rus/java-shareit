package ru.practicum.shareit.item;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);


    private final Map<Long, String> userStub = new HashMap<>();

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
            @RequestBody Map<String, Object> body) {

        if (userId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "X-Sharer-User-Id required"));


        if (!userStub.containsKey(userId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));

        String name = (String) body.get("name");
        String description = (String) body.get("description");
        Boolean available = body.get("available") != null ? Boolean.valueOf(body.get("available").toString()) : null;

        if (name == null || name.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Name required"));
        if (description == null || description.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Description required"));
        if (available == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Available required"));

        Long requestId = body.get("requestId") != null ? Long.valueOf(body.get("requestId").toString()) : null;

        Long id = idGen.getAndIncrement();
        Item item = Item.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .ownerId(userId)
                .requestId(requestId)
                .build();

        items.put(id, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(item));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> update(
            @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> body) {

        if (userId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "X-Sharer-User-Id required"));
        Item item = items.get(itemId);
        if (item == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Item not found"));
        if (!item.getOwnerId().equals(userId))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Access denied"));

        if (body.containsKey("name")) {
            String name = (String) body.get("name");
            if (name == null || name.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Name required"));
            item.setName(name);
        }
        if (body.containsKey("description")) {
            String description = (String) body.get("description");
            if (description == null || description.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Description required"));
            item.setDescription(description);
        }
        if (body.containsKey("available")) {
            Boolean available = Boolean.valueOf(body.get("available").toString());
            if (available == null)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Available required"));
            item.setAvailable(available);
        }
        items.put(itemId, item);
        return ResponseEntity.ok(toDto(item));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<?> get(@PathVariable Long itemId) {
        Item item = items.get(itemId);
        if (item == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Item not found"));
        return ResponseEntity.ok(toDto(item));
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> res = items.values().stream()
                .filter(it -> userId != null && it.getOwnerId().equals(userId))
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(
            @RequestParam(value = "text") String text) {
        if (text == null || text.trim().isEmpty())
            return ResponseEntity.ok(Collections.emptyList());

        String t = text.toLowerCase();
        List<ItemDto> result = items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable())
                        && (item.getName().toLowerCase().contains(t) || item.getDescription().toLowerCase().contains(t)))
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }
}