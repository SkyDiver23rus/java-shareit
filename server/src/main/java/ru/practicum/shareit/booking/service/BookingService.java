package ru.practicum.shareit.server.booking.service;

import ru.practicum.shareit.server.booking.dto.BookingRequestDto;
import ru.practicum.shareit.server.booking.dto.BookingResponseDto;
import ru.practicum.shareit.server.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingRequestDto bookingRequestDto, Long userId);

    BookingResponseDto approve(Long bookingId, Long userId, Boolean approved);

    BookingResponseDto getById(Long bookingId, Long userId);

    List<BookingResponseDto> getAllByBooker(Long bookerId, BookingState state, int from, int size);

    List<BookingResponseDto> getAllByOwner(Long ownerId, BookingState state, int from, int size);
}