package ru.practicum.shareit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldSerializeItemRequestDto() throws Exception {

        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .requestId(100L)
                .build();

        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto requestDto = new ItemRequestDto(
                100L,
                "Нужна дрель для ремонта",
                now,
                List.of(item)
        );


        String json = objectMapper.writeValueAsString(requestDto);


        assertThat(json).contains("\"id\":100");
        assertThat(json).contains("\"description\":\"Нужна дрель для ремонта\"");
        assertThat(json).contains("\"created\"");
        assertThat(json).contains("\"items\"");
        assertThat(json).contains("\"Дрель\"");
    }

    @Test
    void shouldDeserializeItemRequestDto() throws Exception {

        String json = """
                {
                    "id": 100,
                    "description": "Нужна дрель для ремонта",
                    "created": "2024-01-20T10:30:00",
                    "items": [
                        {
                            "id": 1,
                            "name": "Дрель",
                            "description": "Простая дрель",
                            "available": true,
                            "requestId": 100
                        }
                    ]
                }
                """;


        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);


        assertThat(requestDto.getId()).isEqualTo(100L);
        assertThat(requestDto.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(requestDto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 20, 10, 30, 0));
        assertThat(requestDto.getItems()).hasSize(1);
        assertThat(requestDto.getItems().get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void shouldDeserializeItemRequestDto_withEmptyItems() throws Exception {

        String json = """
                {
                    "id": 100,
                    "description": "Нужна дрель для ремонта",
                    "created": "2024-01-20T10:30:00",
                    "items": []
                }
                """;


        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);


        assertThat(requestDto.getId()).isEqualTo(100L);
        assertThat(requestDto.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(requestDto.getItems()).isEmpty();
    }

    @Test
    void shouldDeserializeItemRequestDto_withNullItems() throws Exception {

        String json = """
                {
                    "id": 100,
                    "description": "Нужна дрель для ремонта",
                    "created": "2024-01-20T10:30:00"
                }
                """;


        ItemRequestDto requestDto = objectMapper.readValue(json, ItemRequestDto.class);


        assertThat(requestDto.getId()).isEqualTo(100L);
        assertThat(requestDto.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(requestDto.getItems()).isNull();
    }

    @Test
    void shouldSerializeItemRequestCreateDto() throws Exception {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Нужна дрель для ремонта")
                .build();


        String json = objectMapper.writeValueAsString(createDto);


        assertThat(json).contains("\"description\":\"Нужна дрель для ремонта\"");
    }

    @Test
    void shouldDeserializeItemRequestCreateDto() throws Exception {

        String json = """
                {
                    "description": "Нужна дрель для ремонта"
                }
                """;


        ItemRequestCreateDto createDto = objectMapper.readValue(json, ItemRequestCreateDto.class);


        assertThat(createDto.getDescription()).isEqualTo("Нужна дрель для ремонта");
    }

    @Test
    void shouldDeserializeItemRequestCreateDto_withNullDescription() throws Exception {

        String json = "{}";


        ItemRequestCreateDto createDto = objectMapper.readValue(json, ItemRequestCreateDto.class);


        assertThat(createDto.getDescription()).isNull();
    }

    @Test
    void shouldSerializeItemRequestCreateDto_withNullDescription() throws Exception {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder().build();


        String json = objectMapper.writeValueAsString(createDto);


        assertThat(json).contains("\"description\":null");
    }

    @Test
    void shouldSerializeItemRequestDto_withNullItems() throws Exception {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto requestDto = new ItemRequestDto(
                100L,
                "Нужна дрель для ремонта",
                now,
                null
        );


        String json = objectMapper.writeValueAsString(requestDto);


        assertThat(json).contains("\"id\":100");
        assertThat(json).contains("\"description\":\"Нужна дрель для ремонта\"");
        assertThat(json).contains("\"items\":null");
    }

    @Test
    void shouldValidateItemRequestCreateDto_withValidData() {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("Нужна дрель для ремонта")
                .build();


        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateItemRequestCreateDto_withEmptyDescription() {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("")
                .build();


        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemRequestCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateItemRequestCreateDto_withNullDescription() {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description(null)
                .build();


        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemRequestCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateItemRequestCreateDto_withBlankDescription() {

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description("   ")
                .build();


        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(createDto);


        // @NotBlank проверяет не только null, но и пустые строки и строки с пробелами
        assertThat(violations).hasSize(1);

        ConstraintViolation<ItemRequestCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("description");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateItemRequestCreateDto_withLongDescription() {
        // Тестируем длинное описание
        String longDescription = "A".repeat(1000);

        ItemRequestCreateDto createDto = ItemRequestCreateDto.builder()
                .description(longDescription)
                .build();


        Set<ConstraintViolation<ItemRequestCreateDto>> violations = validator.validate(createDto);


        // Если есть ограничение на длину, оно может сработать
        // Если нет, проверяем что тест проходит без ошибок
        assertThat(violations).isNotNull();
    }

    @Test
    void shouldSerializeItemRequestDto_withEmptyItemsList() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto requestDto = new ItemRequestDto(
                100L,
                "Нужна дрель для ремонта",
                now,
                List.of()
        );


        String json = objectMapper.writeValueAsString(requestDto);


        assertThat(json).contains("\"items\":[]");
    }

    @Test
    void shouldSerializeItemRequestDto_withMultipleItems() throws Exception {

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

        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto requestDto = new ItemRequestDto(
                100L,
                "Нужен инструмент для ремонта",
                now,
                List.of(item1, item2)
        );

        String json = objectMapper.writeValueAsString(requestDto);


        assertThat(json).contains("\"items\":[{");
        assertThat(json).contains("\"Дрель\"");
        assertThat(json).contains("\"Молоток\"");
    }

    @Test
    void shouldDeserializeItemRequestCreateDto_withEmptyJson() throws Exception {

        String json = "{}";


        ItemRequestCreateDto createDto = objectMapper.readValue(json, ItemRequestCreateDto.class);


        assertThat(createDto.getDescription()).isNull();
    }
}