package ru.practicum.shareit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.dto.UserDto;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldSerializeBookingRequestDto() throws Exception {
        // Arrange
        LocalDateTime start = LocalDateTime.of(2024, 1, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 21, 10, 0);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();


        String json = objectMapper.writeValueAsString(requestDto);


        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"start\":\"2024-01-20T10:00:00\"");
        assertThat(json).contains("\"end\":\"2024-01-21T10:00:00\"");
    }

    @Test
    void shouldDeserializeBookingRequestDto() throws Exception {

        String json = """
            {
                "itemId": 1,
                "start": "2024-01-20T10:00:00",
                "end": "2024-01-21T10:00:00"
            }
            """;


        BookingRequestDto requestDto = objectMapper.readValue(json, BookingRequestDto.class);


        assertThat(requestDto.getItemId()).isEqualTo(1L);
        assertThat(requestDto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 20, 10, 0));
        assertThat(requestDto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 21, 10, 0));
    }

    @Test
    void shouldSerializeBookingResponseDto() throws Exception {

        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        UserDto booker = UserDto.builder()
                .id(2L)
                .name("Иван")
                .email("ivan@test.com")
                .build();

        LocalDateTime start = LocalDateTime.of(2024, 1, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 21, 10, 0);

        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(100L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .item(item)
                .booker(booker)
                .build();


        String json = objectMapper.writeValueAsString(responseDto);


        assertThat(json).contains("\"id\":100");
        assertThat(json).contains("\"status\":\"WAITING\"");
        assertThat(json).contains("\"item\"");
        assertThat(json).contains("\"booker\"");
        assertThat(json).contains("\"Дрель\"");
        assertThat(json).contains("\"Иван\"");
    }

    @Test
    void shouldDeserializeBookingResponseDto() throws Exception {

        String json = """
            {
                "id": 100,
                "start": "2024-01-20T10:00:00",
                "end": "2024-01-21T10:00:00",
                "status": "APPROVED",
                "item": {
                    "id": 1,
                    "name": "Дрель",
                    "description": "Простая дрель",
                    "available": true
                },
                "booker": {
                    "id": 2,
                    "name": "Иван",
                    "email": "ivan@test.com"
                }
            }
            """;


        BookingResponseDto responseDto = objectMapper.readValue(json, BookingResponseDto.class);


        assertThat(responseDto.getId()).isEqualTo(100L);
        assertThat(responseDto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(responseDto.getItem().getName()).isEqualTo("Дрель");
        assertThat(responseDto.getBooker().getName()).isEqualTo("Иван");
    }

    @Test
    void shouldValidateBookingRequestDto_withValidData() {

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();


        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(requestDto);


        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateBookingRequestDto_withNullDates() {

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(1L)
                .start(null)
                .end(null)
                .build();


        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(requestDto);


        assertThat(violations).hasSize(2); // start и end не должны быть null
    }
}
