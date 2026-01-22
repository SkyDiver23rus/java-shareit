package ru.practicum.shareit.item.map;

import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public class ItemMapper {

    // Преобразование Item в ItemDto
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

    // Преобразование ItemCreateDto в Item (для создания)
    public static Item toItem(ItemCreateDto dto, User owner) {
        if (dto == null) {
            return null;
        }

        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .build();
    }

    // Преобразование Item в ItemWithBookingsDto (для детального просмотра)
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

    // Преобразование ItemUpdateDto в Item (для обновления)
    public static Item toItem(ItemUpdateDto dto, User owner) {
        if (dto == null) {
            return null;
        }

        return Item.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .build();
    }

    // Обновление Item из ItemUpdateDto
    public static void updateItemFromDto(ItemUpdateDto dto, Item item) {
        if (dto == null || item == null) {
            return;
        }

        if (dto.getName() != null && !dto.getName().isBlank()) {
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            item.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }
    }
}