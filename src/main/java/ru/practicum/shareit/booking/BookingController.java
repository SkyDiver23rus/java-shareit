package ru.practicum.shareit.booking;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BookingDto> add(@RequestBody BookingDto dto) {
        BookingDto saved = service.addBooking(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        BookingDto dto = service.getBooking(id);
        if (dto == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Booking not found"));
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public List<BookingDto> getAll() {
        return service.getAllBookings();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteBooking(id);
        return ResponseEntity.ok().build();
    }
}