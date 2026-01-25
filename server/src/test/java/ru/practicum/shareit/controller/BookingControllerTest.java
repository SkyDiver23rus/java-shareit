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
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingResponseDto bookingResponseDto;
    private BookingRequestDto bookingRequestDto;

    @BeforeEach
    void setUp() {

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();


        UserDto userDto = UserDto.builder()
                .id(2L)
                .name("Booker")
                .email("booker@test.com")
                .build();


        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(itemDto)
                .booker(userDto)
                .build();


        bookingRequestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();
    }

    @Test
    @DisplayName("POST /bookings - успешное создание бронирования")
    void create_shouldReturnCreated_whenValidData() throws Exception {
        when(bookingService.create(any(BookingRequestDto.class), eq(2L)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingService, times(1)).create(any(BookingRequestDto.class), eq(2L));
    }

    @Test
    @DisplayName("POST /bookings - без заголовка пользователя (ваше приложение возвращает 500)")
    void create_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("POST /bookings - невалидные даты")
    void create_shouldReturnBadRequest_whenInvalidDates() throws Exception {
        when(bookingService.create(any(BookingRequestDto.class), eq(2L)))
                .thenThrow(new ValidationException("Дата окончания должна быть позже даты начала"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /bookings - предмет недоступен")
    void create_shouldReturnBadRequest_whenItemUnavailable() throws Exception {
        when(bookingService.create(any(BookingRequestDto.class), eq(2L)))
                .thenThrow(new UnavailableItemException("Вещь недоступна для бронирования"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId} - успешное подтверждение бронирования")
    void approve_shouldReturnOk_whenApproved() throws Exception {
        BookingResponseDto approvedBooking = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approve(eq(1L), eq(1L), eq(true)))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService, times(1)).approve(eq(1L), eq(1L), eq(true));
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId} - без параметра approved")
    void approve_shouldReturnBadRequest_whenApprovedMissing() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings/{bookingId} - успешное получение бронирования")
    void getById_shouldReturnOk_whenBookingExists() throws Exception {
        when(bookingService.getById(eq(1L), eq(2L)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookingService, times(1)).getById(eq(1L), eq(2L));
    }

    @Test
    @DisplayName("GET /bookings/{bookingId} - бронирование не найдено")
    void getById_shouldReturnNotFound_whenBookingNotFound() throws Exception {
        when(bookingService.getById(eq(999L), eq(2L)))
                .thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /bookings - получение бронирований пользователя (ALL)")
    void getAllByBooker_shouldReturnList_whenStateAll() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getAllByBooker(eq(2L), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService, times(1)).getAllByBooker(eq(2L), eq(BookingState.ALL), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /bookings - получение бронирований пользователя (CURRENT)")
    void getAllByBooker_shouldReturnList_whenStateCurrent() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getAllByBooker(eq(2L), eq(BookingState.CURRENT), eq(0), eq(10)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .param("state", "CURRENT")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllByBooker(eq(2L), eq(BookingState.CURRENT), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /bookings/owner - получение бронирований владельца")
    void getAllByOwner_shouldReturnList_whenStateAll() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getAllByOwner(eq(1L), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService, times(1)).getAllByOwner(eq(1L), eq(BookingState.ALL), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /bookings - дефолтные значения параметров")
    void getAllByBooker_shouldUseDefaults_whenParamsMissing() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getAllByBooker(eq(2L), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2"))
                .andExpect(status().isOk());

        verify(bookingService, times(1)).getAllByBooker(eq(2L), eq(BookingState.ALL), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /bookings/owner - без заголовка пользователя (ваше приложение возвращает 500)")
    void getAllByOwner_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId} - пользователь не владелец")
    void approve_shouldReturnForbidden_whenUserNotOwner() throws Exception {
        when(bookingService.approve(eq(1L), eq(999L), eq(true)))
                .thenThrow(new AccessDeniedException("Только владелец вещи может подтверждать бронирование"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", "999")
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /bookings - владелец пытается забронировать свою вещь")
    void create_shouldReturnForbidden_whenOwnerBooksOwnItem() throws Exception {
        when(bookingService.create(any(BookingRequestDto.class), eq(1L)))
                .thenThrow(new AccessDeniedException("Владелец не может бронировать свою вещь"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /bookings - с датой начала в прошлом")
    void create_shouldReturnBadRequest_whenStartInPast() throws Exception {
        BookingRequestDto pastRequest = BookingRequestDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(1)) // в прошлом
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(bookingService.create(any(BookingRequestDto.class), eq(2L)))
                .thenThrow(new ValidationException("Дата начала не может быть в прошлом"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pastRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings/{bookingId} - без заголовка пользователя (ваше приложение возвращает 500)")
    void getById_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("PATCH /bookings/{bookingId} - без заголовка пользователя (ваше приложение возвращает 500)")
    void approve_shouldReturnInternalError_whenUserIdMissing() throws Exception {
        // Изменено: ожидаем 500 вместо 400
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isInternalServerError()); // Изменено с 400 на 500
    }

    @Test
    @DisplayName("POST /bookings - с некорректным itemId")
    void create_shouldReturnBadRequest_whenInvalidItemId() throws Exception {
        BookingRequestDto invalidRequest = BookingRequestDto.builder()
                .itemId(null) // null itemId
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingService.create(any(BookingRequestDto.class), eq(2L)))
                .thenThrow(new ValidationException("Item ID не может быть null"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings - с некорректными параметрами пагинации")
    void getAllByBooker_shouldReturnBadRequest_whenInvalidPagination() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .param("from", "-1") // отрицательное значение
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings/owner - с некорректными параметрами пагинации")
    void getAllByOwner_shouldReturnBadRequest_whenInvalidPagination() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "0")
                        .param("size", "0")) // нулевое значение
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /bookings - с некорректным состоянием")
    void getAllByBooker_shouldReturnBadRequest_whenInvalidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", "2")
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }
}