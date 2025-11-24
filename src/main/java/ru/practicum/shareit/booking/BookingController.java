package ru.practicum.shareit.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @PostMapping
    public ResponseEntity<BookingDto> create(@RequestBody Map<String, Object> body) {
        Long itemId = Long.valueOf(body.get("itemId").toString());
        Long bookerId = Long.valueOf(body.get("bookerId").toString());
        LocalDateTime start = LocalDateTime.parse(body.get("start").toString());
        LocalDateTime end = LocalDateTime.parse(body.get("end").toString());
        String status = (String) body.getOrDefault("status", "WAITING");
        Long id = idGen.getAndIncrement();
        Booking b = Booking.builder()
                .id(id)
                .itemId(itemId)
                .bookerId(bookerId)
                .start(start)
                .end(end)
                .status(status)
                .build();
        bookings.put(id, b);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(b));
    }

    private BookingDto toDto(Booking b) {
        return BookingDto.builder()
                .id(b.getId()).start(b.getStart()).end(b.getEnd())
                .itemId(b.getItemId()).bookerId(b.getBookerId())
                .status(b.getStatus())
                .build();
    }
}