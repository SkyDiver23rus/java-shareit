package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .requestorId(dto.getRequestorId())
                .created(dto.getCreated())
                .build();
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requestorId(request.getRequestorId())
                .created(request.getCreated())
                .build();
    }
}