package ru.practicum.shareit.booking.controller;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.util.HeaderConstants;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponseDto> create(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        BookingResponseDto booking = bookingService.create(bookingRequestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> approve(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        BookingResponseDto booking = bookingService.approve(bookingId, userId, approved);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getById(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @PathVariable Long bookingId) {
        BookingResponseDto booking = bookingService.getById(bookingId, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllByBooker(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        List<BookingResponseDto> bookings = bookingService.getAllByBooker(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getAllByOwner(
            @RequestHeader(HeaderConstants.SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        List<BookingResponseDto> bookings = bookingService.getAllByOwner(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }
}