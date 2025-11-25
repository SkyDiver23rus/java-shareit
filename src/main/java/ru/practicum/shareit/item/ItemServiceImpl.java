package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    public ItemServiceImpl(ItemRepository itemRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    public ItemDto addItem(ItemDto dto, Long ownerId) {
        if (!userService.exists(ownerId))
            throw new IllegalArgumentException("User not found");
        if (dto.getName() == null || dto.getName().isEmpty())
            throw new IllegalArgumentException("Name required");
        if (dto.getDescription() == null || dto.getDescription().isEmpty())
            throw new IllegalArgumentException("Description required");
        if (dto.getAvailable() == null)
            throw new IllegalArgumentException("Available required");
        Item item = ItemMapper.toItem(dto, ownerId);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto dto, Long ownerId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        if (!item.getOwnerId().equals(ownerId))
            throw new IllegalArgumentException("Access denied");
        if (dto.getName() != null && !dto.getName().isEmpty())
            item.setName(dto.getName());
        if (dto.getDescription() != null && !dto.getDescription().isEmpty())
            item.setDescription(dto.getDescription());
        if (dto.getAvailable() != null)
            item.setAvailable(dto.getAvailable());
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return itemRepository.findById(itemId).map(ItemMapper::toItemDto).orElse(null);
    }

    @Override
    public List<ItemDto> getItemsOfUser(Long userId) {
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isEmpty()) return List.of();
        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}