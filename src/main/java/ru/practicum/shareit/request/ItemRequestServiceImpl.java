package ru.practicum.shareit.request;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemRequestServiceImpl implements ItemRequestService {
    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public ItemRequestDto addRequest(ItemRequestDto dto) {
        ItemRequest request = ItemRequestMapper.toItemRequest(dto);
        request.setId(seq.getAndIncrement());
        requests.put(request.getId(), request);
        return ItemRequestMapper.toItemRequestDto(request);
    }

    @Override
    public ItemRequestDto getRequest(Long id) {
        ItemRequest req = requests.get(id);
        if (req == null) return null;
        return ItemRequestMapper.toItemRequestDto(req);
    }

    @Override
    public List<ItemRequestDto> getAll() {
        return requests.values().stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @Override
    public void deleteRequest(Long id) {
        requests.remove(id);
    }
}