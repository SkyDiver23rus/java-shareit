package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
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
    public ItemDto addItem(ItemCreateDto dto, Long ownerId) {
        if (ownerId == null || !userService.exists(ownerId)) {
            throw new NotFoundException("Пользователь не найден с таким id: " + ownerId);
        }
        Item item = ItemMapper.toItemFromCreateDto(dto, ownerId);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemUpdateDto dto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден с id: " + itemId));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Доступ запрещен: Пользователь " + ownerId + " не может вносить изменения " + itemId);
        }

        if (dto.getName() != null) {
            if (dto.getName().isBlank()) {
                throw new IllegalArgumentException("Название не может быть пустым");
            }
            item.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            if (dto.getDescription().isBlank()) {
                throw new IllegalArgumentException("Описание не может быть пустым");
            }
            item.setDescription(dto.getDescription());
        }

        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Предмет не найден с id: " + itemId));
    }

    @Override
    public List<ItemDto> getItemsOfUser(Long userId) {
        if (!userService.exists(userId)) {
            throw new NotFoundException("Пользователь не найден с таким id: " + userId);
        }

        return itemRepository.findAllByOwnerId(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableByText(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}