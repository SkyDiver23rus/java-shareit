package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.model.User;
import ru.practicum.shareit.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void addItem_success() {
        User owner = userRepository.save(new User(null, "Влад", "vlad@test.com"));
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", true, null);

        ItemDto saved = itemService.addItem(dto, owner.getId());

        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Дрель");
        assertThat(saved.getDescription()).isEqualTo("Мощная дрель");
        assertThat(saved.getAvailable()).isTrue();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void updateItem_success() {
        User owner = userRepository.save(new User(null, "Влад", "vlad@test.com"));
        Item item = itemRepository.save(new Item(null, "Дрель", "Старое описание", true, owner, null));

        ItemUpdateDto updateDto = new ItemUpdateDto("Обновлённая дрель", "Новое описание", false);
        ItemDto updated = itemService.updateItem(item.getId(), updateDto, owner.getId());

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Обновлённая дрель");
        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void getItemsOfUser_returnsItems() {
        User owner = userRepository.save(new User(null, "Влад", "vlad@test.com"));
        itemRepository.save(new Item(null, "Молоток", "Тяжёлый молоток", true, owner, null));

        List<ItemWithBookingsDto> items = itemService.getItemsOfUser(owner.getId());

        assertThat(items).isNotEmpty();
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getName()).isEqualTo("Молоток");
        assertThat(items.getFirst().getDescription()).isEqualTo("Тяжёлый молоток");
    }

    @Test
    void search_returnsMatchingItems() {
        User owner = userRepository.save(new User(null, "Влад", "vlad@test.com"));
        itemRepository.save(new Item(null, "Пила", "Острая пила", true, owner, null));

        List<ItemDto> results = itemService.search("Пила");

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getName()).isEqualTo("Пила");
        assertThat(results.getFirst().getDescription()).isEqualTo("Острая пила");
    }

    @Test
    void search_returnsEmptyList_whenNoMatches() {
        User owner = userRepository.save(new User(null, "Влад", "vlad@test.com"));
        itemRepository.save(new Item(null, "Пила", "Острая пила", true, owner, null));

        List<ItemDto> results = itemService.search("Дрель");

        assertThat(results).isEmpty();
    }

    @Test
    void getItem_success() {
        User owner = userRepository.save(new User(null, "Влад", "vlad@test.com"));
        Item item = itemRepository.save(new Item(null, "Дрель", "Мощная дрель", true, owner, null));

        ItemWithBookingsDto found = itemService.getItem(item.getId(), owner.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(item.getId());
        assertThat(found.getName()).isEqualTo("Дрель");
        assertThat(found.getDescription()).isEqualTo("Мощная дрель");
    }

    @Test
    void getItemsOfUser_returnsEmptyList_whenNoItems() {
        User owner = userRepository.save(new User(null, "Влад", "vlad@test.com"));

        List<ItemWithBookingsDto> items = itemService.getItemsOfUser(owner.getId());

        assertThat(items).isEmpty();
    }
}