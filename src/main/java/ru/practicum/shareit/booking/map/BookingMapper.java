package ru.practicum.shareit.booking.map;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.map.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.map.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    public static Booking toBooking(BookingRequestDto dto, Item item, User booker) {
        if (dto == null || item == null || booker == null) {
            return null;
        }

        return Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }

    public static BookingResponseDto toBookingResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(booking.getBooker() != null ? UserMapper.toUserDto(booking.getBooker()) : null)
                .item(booking.getItem() != null ? ItemMapper.toItemDto(booking.getItem()) : null)
                .build();
    }

    public static List<BookingResponseDto> toBookingResponseDtoList(Collection<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return List.of();
        }

        return bookings.stream()
                .map(BookingMapper::toBookingResponseDto)
                .collect(Collectors.toList());
    }
}