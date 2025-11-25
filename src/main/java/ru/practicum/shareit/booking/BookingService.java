package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(BookingDto bookingDto);

    BookingDto getBooking(Long id);

    List<BookingDto> getAllBookings();

    void deleteBooking(Long id);
}