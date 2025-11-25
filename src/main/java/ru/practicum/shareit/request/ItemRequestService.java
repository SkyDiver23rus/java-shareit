package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestDto requestDto);

    ItemRequestDto getRequest(Long id);

    List<ItemRequestDto> getAll();

    void deleteRequest(Long id);
}