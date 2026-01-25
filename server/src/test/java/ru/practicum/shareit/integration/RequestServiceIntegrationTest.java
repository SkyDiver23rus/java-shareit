package ru.practicum.shareit.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.service.UserService;
import ru.practicum.shareit.dto.UserCreateDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class RequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long requesterId;
    private Long anotherUserId;

    @BeforeEach
    void setUp() {
        // Создаем тестовых пользователей перед каждым тестом
        UserCreateDto requester = UserCreateDto.builder()
                .name("Запрашивающий")
                .email("requester@test.com")
                .build();
        requesterId = userService.createUser(requester).getId();

        UserCreateDto anotherUser = UserCreateDto.builder()
                .name("Другой пользователь")
                .email("another@test.com")
                .build();
        anotherUserId = userService.createUser(anotherUser).getId();
    }

    @Test
    @DisplayName("Создание и получение собственных запросов - успешный сценарий")
    void createAndGetOwnRequests_success() {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Нужен молоток")
                .build();


        ItemRequestDto saved = itemRequestService.create(requesterId, createDto);
        List<ItemRequestDto> requests = itemRequestService.getOwnRequests(requesterId);


        assertThat(saved).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Нужен молоток");
        assertThat(saved.getItems()).isEmpty(); // пока нет предметов для запроса

        assertThat(requests).hasSize(1);
        assertThat(requests.get(0).getDescription()).isEqualTo("Нужен молоток");
        assertThat(requests.get(0).getId()).isEqualTo(saved.getId());
        assertThat(requests.get(0).getCreated()).isNotNull();
    }

    @Test
    @DisplayName("Создание нескольких запросов")
    void createMultipleRequests_success() {

        ItemRequestCreateDto request1 = ItemRequestCreateDto.builder()
                .description("Нужен молоток")
                .build();

        ItemRequestCreateDto request2 = ItemRequestCreateDto.builder()
                .description("Нужна дрель")
                .build();


        itemRequestService.create(requesterId, request1);
        itemRequestService.create(requesterId, request2);

        List<ItemRequestDto> requests = itemRequestService.getOwnRequests(requesterId);


        assertThat(requests).hasSize(2);
        assertThat(requests)
                .extracting("description")
                .containsExactlyInAnyOrder("Нужен молоток", "Нужна дрель");
    }

    @Test
    @DisplayName("Получение запросов других пользователей")
    void getAllRequests_success() {

        // Создаем запрос от первого пользователя
        ItemRequestCreateDto request = ItemRequestCreateDto.builder()
                .description("Нужен молоток")
                .build();
        itemRequestService.create(requesterId, request);


        // Второй пользователь получает все запросы
        List<ItemRequestDto> allRequests = itemRequestService.getAllRequests(anotherUserId, 0, 10);


        assertThat(allRequests).hasSize(1);
        assertThat(allRequests.get(0).getDescription()).isEqualTo("Нужен молоток");
    }

    @Test
    @DisplayName("Получение запросов других пользователей с пагинацией")
    void getAllRequests_withPagination() {

        for (int i = 1; i <= 3; i++) {
            ItemRequestCreateDto request = ItemRequestCreateDto.builder()
                    .description("Запрос " + i)
                    .build();
            itemRequestService.create(requesterId, request);
        }

        //олучаем по 2 запроса за раз
        List<ItemRequestDto> firstPage = itemRequestService.getAllRequests(anotherUserId, 0, 2);
        List<ItemRequestDto> secondPage = itemRequestService.getAllRequests(anotherUserId, 2, 2);


        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("Получение запросов других пользователей - пустой результат")
    void getAllRequests_emptyResult() {

        List<ItemRequestDto> allRequests = itemRequestService.getAllRequests(anotherUserId, 0, 10);


        assertThat(allRequests).isEmpty();
    }

    @Test
    @DisplayName("Получение запроса по ID")
    void getRequestById_success() {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Нужен молоток")
                .build();
        ItemRequestDto saved = itemRequestService.create(requesterId, createDto);


        ItemRequestDto found = itemRequestService.getRequestById(requesterId, saved.getId());


        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getDescription()).isEqualTo("Нужен молоток");
    }

    @Test
    @DisplayName("Получение несуществующего запроса")
    void getRequestById_notFound() {

        assertThatThrownBy(() -> itemRequestService.getRequestById(requesterId, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос с ID 999 не найден");
    }

    @Test
    @DisplayName("Создание запроса с добавлением предмета")
    void createRequestWithItem_success() {

        // Создаем запрос
        ItemRequestCreateDto requestDto = ItemRequestCreateDto.builder()
                .description("Нужны инструменты")
                .build();
        ItemRequestDto request = itemRequestService.create(requesterId, requestDto);

        // Создаем предмет для этого запроса
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("Молоток")
                .description("Тяжелый молоток")
                .available(true)
                .requestId(request.getId())
                .build();

        // Второй пользователь создает предмет для запроса
        ItemDto item = itemService.addItem(itemCreateDto, anotherUserId);


        ItemRequestDto foundRequest = itemRequestService.getRequestById(requesterId, request.getId());


        assertThat(foundRequest.getItems()).hasSize(1);
        assertThat(foundRequest.getItems().get(0).getName()).isEqualTo("Молоток");
        assertThat(foundRequest.getItems().get(0).getRequestId()).isEqualTo(request.getId());
    }

    @Test
    @DisplayName("Создание запроса несуществующим пользователем")
    void createRequest_userNotFound() {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Нужен молоток")
                .build();


        assertThatThrownBy(() -> itemRequestService.create(999L, createDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID 999 не найден");
    }

    @Test
    @DisplayName("Получение собственных запросов несуществующим пользователем")
    void getOwnRequests_userNotFound() {

        assertThatThrownBy(() -> itemRequestService.getOwnRequests(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Получение запросов других пользователей несуществующим пользователем")
    void getAllRequests_userNotFound() {

        assertThatThrownBy(() -> itemRequestService.getAllRequests(999L, 0, 10))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Собственные запросы не показываются в getAllRequests")
    void getAllRequests_excludeOwnRequests() {

        // Создаем запрос от первого пользователя
        ItemRequestCreateDto request = ItemRequestCreateDto.builder()
                .description("Мой запрос")
                .build();
        itemRequestService.create(requesterId, request);


        // Первый пользователь пытается получить все запросы
        List<ItemRequestDto> allRequests = itemRequestService.getAllRequests(requesterId, 0, 10);


        assertThat(allRequests).isEmpty();
    }

    @Test
    @DisplayName("Запрос с несколькими предметами")
    void requestWithMultipleItems_success() {

        ItemRequestCreateDto requestDto = ItemRequestCreateDto.builder()
                .description("Нужны инструменты")
                .build();
        ItemRequestDto request = itemRequestService.create(requesterId, requestDto);

        // Создаем несколько предметов для этого запроса
        ItemCreateDto item1 = ItemCreateDto.builder()
                .name("Молоток")
                .description("Тяжелый молоток")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemCreateDto item2 = ItemCreateDto.builder()
                .name("Отвертка")
                .description("Крестовая отвертка")
                .available(true)
                .requestId(request.getId())
                .build();

        itemService.addItem(item1, anotherUserId);
        itemService.addItem(item2, anotherUserId);


        ItemRequestDto foundRequest = itemRequestService.getRequestById(requesterId, request.getId());


        assertThat(foundRequest.getItems()).hasSize(2);
        assertThat(foundRequest.getItems())
                .extracting("name")
                .containsExactlyInAnyOrder("Молоток", "Отвертка");
    }

    @Test
    @DisplayName("Запросы сортируются по дате создания (от новых к старым)")
    void requestsSortedByCreationDate() {

        ItemRequestCreateDto request1 = ItemRequestCreateDto.builder()
                .description("Первый запрос")
                .build();

        ItemRequestCreateDto request2 = ItemRequestCreateDto.builder()
                .description("Второй запрос")
                .build();

        ItemRequestDto firstRequest = itemRequestService.create(requesterId, request1);

        // Небольшая задержка для разного времени создания
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ItemRequestDto secondRequest = itemRequestService.create(requesterId, request2);


        List<ItemRequestDto> requests = itemRequestService.getOwnRequests(requesterId);


        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getId()).isEqualTo(secondRequest.getId()); // второй создан позже
        assertThat(requests.get(1).getId()).isEqualTo(firstRequest.getId());  // первый создан раньше
    }
}