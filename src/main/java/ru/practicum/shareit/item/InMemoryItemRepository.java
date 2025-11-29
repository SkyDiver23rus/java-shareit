package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(seq.getAndIncrement());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long userId) {
        return items.values().stream()
                .filter(i -> Objects.equals(i.getOwnerId(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        String t = text.toLowerCase();
        return items.values().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable())
                        && (i.getName().toLowerCase().contains(t) || i.getDescription().toLowerCase().contains(t)))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }
}