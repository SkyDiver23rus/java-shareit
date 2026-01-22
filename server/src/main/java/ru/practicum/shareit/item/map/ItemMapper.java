package ru.practicum.shareit.item.map;

import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static ItemWithBookingsDto toItemWithBookingsDto(
            Item item,
            ItemWithBookingsDto.BookingShortDto lastBooking,
            ItemWithBookingsDto.BookingShortDto nextBooking,
            List<CommentResponseDto> comments) {

        if (item == null) {
            return null;
        }

        return ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments != null ? comments : List.of())
                .build();
    }
}