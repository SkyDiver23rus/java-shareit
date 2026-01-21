package ru.practicum.shareit.server.request.service;

import ru.practicum.shareit.server.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestDto requestDto, Long userId);

    ItemRequestDto getRequest(Long id, Long userId);

    List<ItemRequestDto> getAll(Long userId);

    void deleteRequest(Long id, Long userId);
}