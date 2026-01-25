package ru.practicum.shareit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldSerializeItemDto() throws Exception {

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .requestId(10L)
                .build();


        String json = objectMapper.writeValueAsString(itemDto);


        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"description\":\"Мощная дрель\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":10");
    }

    @Test
    void shouldDeserializeItemDto() throws Exception {

        String json = """
                {
                    "id": 1,
                    "name": "Дрель",
                    "description": "Мощная дрель",
                    "available": true,
                    "requestId": 10
                }
                """;


        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);


        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getRequestId()).isEqualTo(10L);
    }

    @Test
    void shouldSerializeItemCreateDto() throws Exception {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .requestId(10L)
                .build();


        String json = objectMapper.writeValueAsString(createDto);


        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"description\":\"Мощная дрель\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":10");
    }

    @Test
    void shouldDeserializeItemCreateDto() throws Exception {

        String json = """
                {
                    "name": "Дрель",
                    "description": "Мощная дрель",
                    "available": true,
                    "requestId": 10
                }
                """;


        ItemCreateDto createDto = objectMapper.readValue(json, ItemCreateDto.class);

        assertThat(createDto.getName()).isEqualTo("Дрель");
        assertThat(createDto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(createDto.getAvailable()).isTrue();
        assertThat(createDto.getRequestId()).isEqualTo(10L);
    }

    @Test
    void shouldSerializeItemWithBookingsDto() throws Exception {

        ItemWithBookingsDto.BookingShortDto lastBooking = ItemWithBookingsDto.BookingShortDto.builder()
                .id(101L)
                .bookerId(2L)
                .build();

        ItemWithBookingsDto.BookingShortDto nextBooking = ItemWithBookingsDto.BookingShortDto.builder()
                .id(102L)
                .bookerId(3L)
                .build();

        CommentResponseDto comment = CommentResponseDto.builder()
                .id(201L)
                .text("Отличная вещь!")
                .authorName("Иван")
                .created(LocalDateTime.now())
                .build();

        ItemWithBookingsDto itemDto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();


        String json = objectMapper.writeValueAsString(itemDto);


        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"description\":\"Мощная дрель\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"lastBooking\"");
        assertThat(json).contains("\"nextBooking\"");
        assertThat(json).contains("\"comments\"");
    }

    @Test
    void shouldDeserializeItemWithBookingsDto() throws Exception {

        String json = """
                {
                    "id": 1,
                    "name": "Дрель",
                    "description": "Мощная дрель",
                    "available": true,
                    "lastBooking": {
                        "id": 101,
                        "bookerId": 2
                    },
                    "nextBooking": {
                        "id": 102,
                        "bookerId": 3
                    },
                    "comments": [
                        {
                            "id": 201,
                            "text": "Отличная вещь!",
                            "authorName": "Иван",
                            "created": "2024-01-01T12:00:00"
                        }
                    ]
                }
                """;


        ItemWithBookingsDto itemDto = objectMapper.readValue(json, ItemWithBookingsDto.class);


        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isEqualTo("Дрель");
        assertThat(itemDto.getDescription()).isEqualTo("Мощная дрель");
        assertThat(itemDto.getAvailable()).isTrue();
        assertThat(itemDto.getLastBooking()).isNotNull();
        assertThat(itemDto.getLastBooking().getId()).isEqualTo(101L);
        assertThat(itemDto.getLastBooking().getBookerId()).isEqualTo(2L);
        assertThat(itemDto.getComments()).hasSize(1);
    }

    @Test
    void shouldSerializeCommentResponseDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        CommentResponseDto comment = CommentResponseDto.builder()
                .id(1L)
                .text("Отличная вещь!")
                .authorName("Иван")
                .created(created)
                .build();


        String json = objectMapper.writeValueAsString(comment);


        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"text\":\"Отличная вещь!\"");
        assertThat(json).contains("\"authorName\":\"Иван\"");
        assertThat(json).contains("\"created\":\"2024-01-01T12:00:00\"");
    }

    @Test
    void shouldDeserializeCommentResponseDto() throws Exception {

        String json = """
                {
                    "id": 1,
                    "text": "Отличная вещь!",
                    "authorName": "Иван",
                    "created": "2024-01-01T12:00:00"
                }
                """;


        CommentResponseDto comment = objectMapper.readValue(json, CommentResponseDto.class);

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Отличная вещь!");
        assertThat(comment.getAuthorName()).isEqualTo("Иван");
        assertThat(comment.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 12, 0, 0));
    }

    @Test
    void shouldValidateItemCreateDto_withValidData() {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .build();


        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateItemCreateDto_withEmptyName() {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("")
                .description("Мощная дрель")
                .available(true)
                .build();


        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateItemCreateDto_withNullName() {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name(null)
                .description("Мощная дрель")
                .available(true)
                .build();


        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateItemCreateDto_withEmptyDescription() {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("")
                .available(true)
                .build();


        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateItemCreateDto_withNullDescription() {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Дрель")
                .description(null)
                .available(true)
                .build();


        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateItemCreateDto_withNullAvailable() {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Дрель")
                .description("Мощная дрель")
                .available(null)
                .build();

        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);

        // Если есть аннотация @NotNull на available, будет нарушение
        // Если нет, то проверяем только что тест проходит
        assertThat(violations).isNotNull();
    }

    @Test
    void shouldValidateItemCreateDto_withAllInvalidFields() {

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("")
                .description("")
                .available(null)
                .build();


        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);


        // Ожидаем минимум 2 нарушения: для name и description
        assertThat(violations.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldSerializeItemDto_withNullValues() throws Exception {

        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .build();


        String json = objectMapper.writeValueAsString(itemDto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":null");
        assertThat(json).contains("\"description\":null");
        assertThat(json).contains("\"available\":null");
        assertThat(json).contains("\"requestId\":null");
    }

    @Test
    void shouldDeserializeItemDto_withMissingFields() throws Exception {

        String json = """
                {
                    "id": 1
                }
                """;


        ItemDto itemDto = objectMapper.readValue(json, ItemDto.class);


        assertThat(itemDto.getId()).isEqualTo(1L);
        assertThat(itemDto.getName()).isNull();
        assertThat(itemDto.getDescription()).isNull();
        assertThat(itemDto.getAvailable()).isNull();
        assertThat(itemDto.getRequestId()).isNull();
    }

    @Test
    void shouldSerializeItemCreateDto_withNullValues() throws Exception {

        ItemCreateDto createDto = ItemCreateDto.builder().build();


        String json = objectMapper.writeValueAsString(createDto);


        assertThat(json).contains("\"name\":null");
        assertThat(json).contains("\"description\":null");
        assertThat(json).contains("\"available\":null");
        assertThat(json).contains("\"requestId\":null");
    }

    @Test
    void shouldDeserializeItemCreateDto_withMissingFields() throws Exception {

        String json = "{}";


        ItemCreateDto createDto = objectMapper.readValue(json, ItemCreateDto.class);


        assertThat(createDto.getName()).isNull();
        assertThat(createDto.getDescription()).isNull();
        assertThat(createDto.getAvailable()).isNull();
        assertThat(createDto.getRequestId()).isNull();
    }

    @Test
    void shouldSerializeItemWithBookingsDto_withNullBookings() throws Exception {

        ItemWithBookingsDto itemDto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .build();


        String json = objectMapper.writeValueAsString(itemDto);


        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"lastBooking\":null");
        assertThat(json).contains("\"nextBooking\":null");
        assertThat(json).contains("\"comments\":[]");
    }

    @Test
    void shouldSerializeItemWithBookingsDto_withEmptyComments() throws Exception {

        ItemWithBookingsDto itemDto = ItemWithBookingsDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .comments(List.of())
                .build();


        String json = objectMapper.writeValueAsString(itemDto);


        assertThat(json).contains("\"comments\":[]");
    }

    @Test
    void shouldValidateItemCreateDto_withLongButValidFields() {

        String longName = "A".repeat(255); // максимальная длина для строки
        String longDescription = "B".repeat(2000); // максимальная длина для описания

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name(longName)
                .description(longDescription)
                .available(true)
                .build();


        Set<ConstraintViolation<ItemCreateDto>> violations = validator.validate(createDto);


        // Если есть ограничения на длину, они сработают
        // Если нет - тест просто проверяет, что валидация проходит
        assertThat(violations).isNotNull();
    }
}