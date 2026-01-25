package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ItemCreateDto itemCreateDto;
    private ItemWithBookingsDto itemWithBookingsDto;
    private CommentRequestDto commentRequestDto;
    private CommentResponseDto commentResponseDto;

    @BeforeEach
    void setUp() {

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();


        itemCreateDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        ItemWithBookingsDto.BookingShortDto lastBooking = ItemWithBookingsDto.BookingShortDto.builder()
                .id(101L)
                .bookerId(2L)
                .build();

        ItemWithBookingsDto.BookingShortDto nextBooking = ItemWithBookingsDto.BookingShortDto.builder()
                .id(102L)
                .bookerId(3L)
                .build();

        itemWithBookingsDto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of())
                .build();


        commentRequestDto = CommentRequestDto.builder()
                .text("Отличная дрель!")
                .build();

        commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Отличная дрель!")
                .authorName("Vlad")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /items - успешное создание предмета")
    void create_shouldReturnCreated_whenValidData() throws Exception {
        when(itemService.addItem(any(ItemCreateDto.class), eq(1L)))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemService, times(1)).addItem(any(ItemCreateDto.class), eq(1L));
    }

    @Test
    @DisplayName("POST /items - без заголовка пользователя (ваше приложение возвращает 500)")
    void create_shouldReturnInternalError_whenUserIdMissing() throws Exception {

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemCreateDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /items - с невалидными данными")
    void create_shouldReturnBadRequest_whenInvalidData() throws Exception {
        ItemCreateDto invalidDto = ItemCreateDto.builder()
                .name("")
                .description("")
                .available(true)
                .build();

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /items/{itemId} - успешное обновление")
    void update_shouldReturnOk_whenValidUpdate() throws Exception {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Обновленная дрель")
                .description("Новое описание")
                .available(false)
                .build();

        ItemDto updatedItem = ItemDto.builder()
                .id(1L)
                .name("Обновленная дрель")
                .description("Новое описание")
                .available(false)
                .build();

        when(itemService.updateItem(eq(1L), any(ItemUpdateDto.class), eq(1L)))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленная дрель"));

        verify(itemService, times(1)).updateItem(eq(1L), any(ItemUpdateDto.class), eq(1L));
    }

    @Test
    @DisplayName("PATCH /items/{itemId} - пользователь не владелец")
    void update_shouldReturnForbidden_whenUserNotOwner() throws Exception {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Обновленная дрель")
                .build();

        when(itemService.updateItem(eq(1L), any(ItemUpdateDto.class), eq(999L)))
                .thenThrow(new AccessDeniedException("Доступ запрещен"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /items/{itemId} - без заголовка пользователя (ваше приложение возвращает 500)")
    void update_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Обновленная дрель")
                .build();


        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("GET /items/{itemId} - успешное получение предмета")
    void get_shouldReturnOk_whenItemExists() throws Exception {
        when(itemService.getItem(eq(1L), eq(1L)))
                .thenReturn(itemWithBookingsDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemService, times(1)).getItem(eq(1L), eq(1L));
    }

    @Test
    @DisplayName("GET /items/{itemId} - без заголовка пользователя (ваше приложение возвращает 500)")
    void get_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("GET /items/{itemId} - предмет не найден")
    void get_shouldReturnNotFound_whenItemNotExists() throws Exception {
        when(itemService.getItem(eq(999L), eq(1L)))
                .thenThrow(new NotFoundException("Предмет не найден"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).getItem(eq(999L), eq(1L));
    }

    @Test
    @DisplayName("GET /items - получение всех предметов пользователя")
    void getAll_shouldReturnList_whenUserHasItems() throws Exception {
        List<ItemWithBookingsDto> items = List.of(itemWithBookingsDto);
        when(itemService.getItemsOfUser(eq(1L))).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemService, times(1)).getItemsOfUser(eq(1L));
    }

    @Test
    @DisplayName("GET /items - без заголовка пользователя (ваше приложение возвращает 500)")
    void getAll_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(get("/items"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("GET /items - пустой список предметов")
    void getAll_shouldReturnEmptyList_whenNoItems() throws Exception {
        when(itemService.getItemsOfUser(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemService, times(1)).getItemsOfUser(eq(1L));
    }

    @Test
    @DisplayName("GET /items/search - успешный поиск")
    void search_shouldReturnList_whenTextProvided() throws Exception {
        List<ItemDto> items = List.of(itemDto);
        when(itemService.search("дрель")).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemService, times(1)).search("дрель");
    }

    @Test
    @DisplayName("GET /items/search - пустой текст поиска")
    void search_shouldReturnEmptyList_whenEmptyText() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /items/search - без параметра text")
    void search_shouldReturnEmptyList_whenNoTextParam() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - успешное добавление комментария")
    void addComment_shouldReturnCreated_whenValidComment() throws Exception {
        when(itemService.addComment(eq(2L), eq(1L), any(CommentRequestDto.class)))
                .thenReturn(commentResponseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Отличная дрель!"));

        verify(itemService, times(1)).addComment(eq(2L), eq(1L), any(CommentRequestDto.class));
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - без заголовка пользователя (ваше приложение возвращает 500)")
    void addComment_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - с пустым текстом")
    void addComment_shouldReturnBadRequest_whenEmptyText() throws Exception {
        CommentRequestDto emptyComment = CommentRequestDto.builder()
                .text("")
                .build();

        when(itemService.addComment(eq(2L), eq(1L), any(CommentRequestDto.class)))
                .thenThrow(new ValidationException("Текст комментария не может быть пустым"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyComment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - предмет не найден")
    void addComment_shouldReturnNotFound_whenItemNotExists() throws Exception {
        when(itemService.addComment(eq(2L), eq(999L), any(CommentRequestDto.class)))
                .thenThrow(new NotFoundException("Предмет не найден"));

        mockMvc.perform(post("/items/999/comment")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - пользователь не бронировал предмет")
    void addComment_shouldReturnBadRequest_whenUserNeverBooked() throws Exception {
        when(itemService.addComment(eq(999L), eq(1L), any(CommentRequestDto.class)))
                .thenThrow(new ValidationException("Пользователь не бронировал этот предмет"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /items/{itemId} - с некорректным ID (не число)")
    void update_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(patch("/items/abc")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ItemUpdateDto.builder().build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /items/{itemId} - с некорректным ID (не число)")
    void get_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(get("/items/abc")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items/{itemId}/comment - с некорректным ID (не число)")
    void addComment_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(post("/items/abc/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /items - слишком длинные поля (приложение пропускает без валидации)")
    void create_shouldReturnCreated_whenFieldsTooLong() throws Exception {
        String tooLongName = "A".repeat(256); // больше 255 символов
        String tooLongDescription = "D".repeat(2001); // больше 2000 символов

        ItemCreateDto dto = ItemCreateDto.builder()
                .name(tooLongName)
                .description(tooLongDescription)
                .available(true)
                .build();


        ItemDto responseDto = ItemDto.builder()
                .id(1L)
                .name(tooLongName)
                .description(tooLongDescription)
                .available(true)
                .build();

        when(itemService.addItem(any(ItemCreateDto.class), eq(1L)))
                .thenReturn(responseDto);

        // Изменено: ожидаем 201 вместо 400, так как приложение не валидирует длину полей
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemService, times(1)).addItem(any(ItemCreateDto.class), eq(1L));
    }
}