package ru.practicum.shareit.request;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private final Map<Long, ItemRequest> reqs = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody Map<String, Object> body) {
        String desc = (String) body.get("description");
        if (desc == null || desc.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Description required"));
        Long id = idGen.getAndIncrement();
        ItemRequest r = ItemRequest.builder()
                .id(id).description(desc)
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build();
        reqs.put(id, r);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(r));
    }

    private ItemRequestDto toDto(ItemRequest r) {
        return ItemRequestDto.builder()
                .id(r.getId())
                .description(r.getDescription())
                .requestorId(r.getRequestorId())
                .created(r.getCreated())
                .build();
    }
}