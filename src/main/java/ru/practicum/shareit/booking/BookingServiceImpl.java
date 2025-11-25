package ru.practicum.shareit.booking;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BookingServiceImpl implements BookingService {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public BookingDto addBooking(BookingDto bookingDto) {
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setId(seq.getAndIncrement());
        bookings.put(booking.getId(), booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBooking(Long id) {
        Booking booking = bookings.get(id);
        if (booking == null) return null;
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllBookings() {
        return bookings.values().stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public void deleteBooking(Long id) {
        bookings.remove(id);
    }
}