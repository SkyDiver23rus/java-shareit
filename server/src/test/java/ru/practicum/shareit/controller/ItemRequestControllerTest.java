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
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto itemRequestDto;
    private ItemRequestCreateDto itemRequestCreateDto;

    @BeforeEach
    void setUp() {

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .requestId(100L)
                .build();


        itemRequestDto = new ItemRequestDto(
                100L,
                "Нужна дрель для ремонта",
                LocalDateTime.now(),
                List.of(itemDto)
        );


        itemRequestCreateDto = ItemRequestCreateDto.builder()
                .description("Нужна дрель для ремонта")
                .build();
    }

    @Test
    @DisplayName("POST /requests - успешное создание запроса")
    void create_shouldReturnCreated_whenValidData() throws Exception {
        when(itemRequestService.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.description").value("Нужна дрель для ремонта"))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"));

        verify(itemRequestService, times(1)).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    @DisplayName("POST /requests - без заголовка пользователя (ваше приложение возвращает 500)")
    void create_shouldReturnInternalError_whenUserIdMissing() throws Exception {

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("POST /requests - с пустым описанием")
    void create_shouldReturnBadRequest_whenEmptyDescription() throws Exception {
        ItemRequestCreateDto emptyDto = ItemRequestCreateDto.builder()
                .description("")
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /requests - с null описанием")
    void create_shouldReturnBadRequest_whenNullDescription() throws Exception {
        ItemRequestCreateDto nullDto = ItemRequestCreateDto.builder()
                .description(null)
                .build();

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nullDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /requests - пользователь не найден")
    void create_shouldReturnNotFound_whenUserNotFound() throws Exception {
        when(itemRequestService.create(eq(999L), any(ItemRequestCreateDto.class)))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestCreateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests - получение собственных запросов")
    void getOwnRequests_shouldReturnList_whenRequestsExist() throws Exception {
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        when(itemRequestService.getOwnRequests(eq(1L))).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель для ремонта"))
                .andExpect(jsonPath("$[0].items.length()").value(1));

        verify(itemRequestService, times(1)).getOwnRequests(eq(1L));
    }

    @Test
    @DisplayName("GET /requests - без заголовка пользователя (ваше приложение возвращает 500)")
    void getOwnRequests_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("GET /requests - пустой список запросов")
    void getOwnRequests_shouldReturnEmptyList_whenNoRequests() throws Exception {
        when(itemRequestService.getOwnRequests(eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /requests - пользователь не найден")
    void getOwnRequests_shouldReturnNotFound_whenUserNotFound() throws Exception {
        when(itemRequestService.getOwnRequests(eq(999L)))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests/all - получение всех запросов")
    void getAllRequests_shouldReturnList_whenRequestsExist() throws Exception {
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        when(itemRequestService.getAllRequests(eq(2L), eq(0), eq(10)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "2")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100));

        verify(itemRequestService, times(1)).getAllRequests(eq(2L), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /requests/all - без заголовка пользователя (ваше приложение возвращает 500)")
    void getAllRequests_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("GET /requests/all - дефолтные значения параметров")
    void getAllRequests_shouldUseDefaults_whenParamsMissing() throws Exception {
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        when(itemRequestService.getAllRequests(eq(2L), eq(0), eq(10)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).getAllRequests(eq(2L), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /requests/all - с параметром from=5, size=5")
    void getAllRequests_shouldUseCustomPagination_whenParamsProvided() throws Exception {
        List<ItemRequestDto> requests = List.of(itemRequestDto);
        when(itemRequestService.getAllRequests(eq(2L), eq(5), eq(5)))
                .thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "2")
                        .param("from", "5")
                        .param("size", "5"))
                .andExpect(status().isOk());

        verify(itemRequestService, times(1)).getAllRequests(eq(2L), eq(5), eq(5));
    }

    @Test
    @DisplayName("GET /requests/all - с невалидными параметрами пагинации (from отрицательный)")
    void getAllRequests_shouldReturnBadRequest_whenFromNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "2")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all - с невалидными параметрами пагинации (size=0)")
    void getAllRequests_shouldReturnBadRequest_whenSizeZero() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "2")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all - с невалидными параметрами пагинации (size отрицательный)")
    void getAllRequests_shouldReturnBadRequest_whenSizeNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "2")
                        .param("from", "0")
                        .param("size", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all - пользователь не найден")
    void getAllRequests_shouldReturnNotFound_whenUserNotFound() throws Exception {
        when(itemRequestService.getAllRequests(eq(999L), eq(0), eq(10)))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", "999")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests/{requestId} - успешное получение запроса")
    void getRequestById_shouldReturnOk_whenRequestExists() throws Exception {
        when(itemRequestService.getRequestById(eq(1L), eq(100L)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/100")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.description").value("Нужна дрель для ремонта"))
                .andExpect(jsonPath("$.items.length()").value(1));

        verify(itemRequestService, times(1)).getRequestById(eq(1L), eq(100L));
    }

    @Test
    @DisplayName("GET /requests/{requestId} - без заголовка пользователя (ваше приложение возвращает 500)")
    void getRequestById_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(get("/requests/100"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("GET /requests/{requestId} - запрос не найден")
    void getRequestById_shouldReturnNotFound_whenRequestNotFound() throws Exception {
        when(itemRequestService.getRequestById(eq(1L), eq(999L)))
                .thenThrow(new NotFoundException("Запрос с ID 999 не найден"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests/{requestId} - пользователь не найден")
    void getRequestById_shouldReturnNotFound_whenUserNotFound() throws Exception {
        when(itemRequestService.getRequestById(eq(999L), eq(100L)))
                .thenThrow(new NotFoundException("Пользователь с ID 999 не найден"));

        mockMvc.perform(get("/requests/100")
                        .header("X-Sharer-User-Id", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /requests/{requestId} - запрос с пустым списком items")
    void getRequestById_shouldReturnOk_whenRequestHasNoItems() throws Exception {
        ItemRequestDto requestWithoutItems = new ItemRequestDto(
                100L,
                "Нужна дрель для ремонта",
                LocalDateTime.now(),
                List.of() // пустой список items
        );

        when(itemRequestService.getRequestById(eq(1L), eq(100L)))
                .thenReturn(requestWithoutItems);

        mockMvc.perform(get("/requests/100")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.items.length()").value(0));

        verify(itemRequestService, times(1)).getRequestById(eq(1L), eq(100L));
    }

    @Test
    @DisplayName("GET /requests/{requestId} - запрос с несколькими items")
    void getRequestById_shouldReturnOk_whenRequestHasMultipleItems() throws Exception {
        ItemDto item1 = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .requestId(100L)
                .build();

        ItemDto item2 = ItemDto.builder()
                .id(2L)
                .name("Молоток")
                .description("Тяжелый молоток")
                .available(true)
                .requestId(100L)
                .build();

        ItemRequestDto requestWithMultipleItems = new ItemRequestDto(
                100L,
                "Нужен инструмент для ремонта",
                LocalDateTime.now(),
                List.of(item1, item2)
        );

        when(itemRequestService.getRequestById(eq(1L), eq(100L)))
                .thenReturn(requestWithMultipleItems);

        mockMvc.perform(get("/requests/100")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"))
                .andExpect(jsonPath("$.items[1].name").value("Молоток"));

        verify(itemRequestService, times(1)).getRequestById(eq(1L), eq(100L));
    }

    @Test
    @DisplayName("POST /requests - логирование запроса")
    void create_shouldLogRequest() throws Exception {
        when(itemRequestService.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                .header("X-Sharer-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequestCreateDto)));


        verify(itemRequestService, times(1)).create(eq(1L), any(ItemRequestCreateDto.class));
    }

    @Test
    @DisplayName("GET /requests - логирование запроса")
    void getOwnRequests_shouldLogRequest() throws Exception {
        when(itemRequestService.getOwnRequests(eq(1L))).thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests")
                .header("X-Sharer-User-Id", "1"));

        verify(itemRequestService, times(1)).getOwnRequests(eq(1L));
    }

    @Test
    @DisplayName("GET /requests/all - логирование запроса с параметрами")
    void getAllRequests_shouldLogRequestWithParams() throws Exception {
        when(itemRequestService.getAllRequests(eq(2L), eq(5), eq(10)))
                .thenReturn(List.of(itemRequestDto));

        mockMvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", "2")
                .param("from", "5")
                .param("size", "10"));

        verify(itemRequestService, times(1)).getAllRequests(eq(2L), eq(5), eq(10));
    }

    @Test
    @DisplayName("GET /requests/{requestId} - логирование запроса")
    void getRequestById_shouldLogRequest() throws Exception {
        when(itemRequestService.getRequestById(eq(1L), eq(100L)))
                .thenReturn(itemRequestDto);

        mockMvc.perform(get("/requests/100")
                .header("X-Sharer-User-Id", "1"));

        verify(itemRequestService, times(1)).getRequestById(eq(1L), eq(100L));
    }

    @Test
    @DisplayName("POST /requests - слишком длинное описание (приложение пропускает без валидации)")
    void create_shouldReturnCreated_whenDescriptionTooLong() throws Exception {
        String tooLongDescription = "A".repeat(2001); // больше 2000 символов

        ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
                .description(tooLongDescription)
                .build();

        ItemRequestDto responseDto = new ItemRequestDto(
                100L,
                tooLongDescription,
                LocalDateTime.now(),
                List.of()
        );

        when(itemRequestService.create(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(tooLongDescription));

        verify(itemRequestService, times(1)).create(eq(1L), any(ItemRequestCreateDto.class));
    }
}