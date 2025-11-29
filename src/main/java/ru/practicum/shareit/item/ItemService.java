package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemCreateDto dto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemUpdateDto dto, Long ownerId);

    ItemDto getItem(Long itemId);

    List<ItemDto> getItemsOfUser(Long userId);

    List<ItemDto> search(String text);
}