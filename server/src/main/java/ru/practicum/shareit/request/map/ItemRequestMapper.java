package ru.practicum.shareit.request.map;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest request, List<ItemDto> items) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(items);
        return dto;
    }
}