package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemCreateDto dto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemUpdateDto dto, Long ownerId);

    ItemWithBookingsDto getItem(Long itemId, Long userId);

    List<ItemWithBookingsDto> getItemsOfUser(Long userId);

    List<ItemDto> search(String text);

    CommentResponseDto addComment(Long userId, Long itemId, CommentRequestDto requestDto);
}