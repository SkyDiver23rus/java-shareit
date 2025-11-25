package ru.practicum.shareit.request;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {
    private final ItemRequestService service;

    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ItemRequestDto> add(@RequestBody ItemRequestDto dto) {
        ItemRequestDto saved = service.addRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        ItemRequestDto dto = service.getRequest(id);
        if (dto == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "ItemRequest not found"));
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public List<ItemRequestDto> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteRequest(id);
        return ResponseEntity.ok().build();
    }
}