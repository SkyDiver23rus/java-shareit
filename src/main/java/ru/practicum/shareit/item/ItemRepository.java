package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.*;

public interface ItemRepository {
    Item save(Item item);

    Optional<Item> findById(Long id);

    List<Item> findAllByOwnerId(Long userId);

    List<Item> searchAvailableByText(String text);

    void deleteById(Long id);

    List<Item> findAll();
}