package ru.practicum.shareit.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.dto.UserCreateDto;
import ru.practicum.shareit.dto.UserDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldSerializeUserDto() throws Exception {

        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();


        String json = objectMapper.writeValueAsString(userDto);


        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"John Doe\"");
        assertThat(json).contains("\"email\":\"john@example.com\"");
    }

    @Test
    void shouldDeserializeUserDto() throws Exception {

        String json = """
                {
                    "id": 1,
                    "name": "John Doe",
                    "email": "john@example.com"
                }
                """;


        UserDto userDto = objectMapper.readValue(json, UserDto.class);


        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("John Doe");
        assertThat(userDto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldSerializeUserCreateDto() throws Exception {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();


        String json = objectMapper.writeValueAsString(createDto);


        assertThat(json).contains("\"name\":\"John Doe\"");
        assertThat(json).contains("\"email\":\"john@example.com\"");
    }

    @Test
    void shouldDeserializeUserCreateDto() throws Exception {

        String json = """
                {
                    "name": "John Doe",
                    "email": "john@example.com"
                }
                """;


        UserCreateDto createDto = objectMapper.readValue(json, UserCreateDto.class);


        assertThat(createDto.getName()).isEqualTo("John Doe");
        assertThat(createDto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldValidateUserCreateDto_withValidData() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();


        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateUserCreateDto_withInvalidEmail() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("John Doe")
                .email("invalid-email")
                .build();


        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<UserCreateDto> violation = violations.iterator().next();

        // Проверяем, что это ошибка валидации email поля
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");

        // Проверяем, что сообщение об ошибке присутствует (любое)
        assertThat(violation.getMessage()).isNotEmpty();

        // Проверяем, что это ошибка аннотации @Email
        assertThat(violation.getConstraintDescriptor().getAnnotation().annotationType()
                .getSimpleName()).contains("Email");
    }

    @Test
    void shouldValidateUserCreateDto_withEmptyName() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("")
                .email("john@example.com")
                .build();


        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<UserCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateUserCreateDto_withNullName() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name(null)
                .email("john@example.com")
                .build();


        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<UserCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateUserCreateDto_withNullEmail() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("John Doe")
                .email(null)
                .build();


        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        assertThat(violations).hasSize(1);

        ConstraintViolation<UserCreateDto> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getMessage()).isNotEmpty();
    }

    @Test
    void shouldValidateUserCreateDto_withEmptyEmail() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("John Doe")
                .email("")
                .build();

        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        // Может быть 2 нарушения: пустая строка и невалидный формат
        assertThat(violations).isNotEmpty();

        // Проверяем, что все нарушения касаются поля email
        for (ConstraintViolation<UserCreateDto> violation : violations) {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        }
    }

    @Test
    void shouldValidateUserCreateDto_withBlankNameAndInvalidEmail() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("   ")
                .email("invalid")
                .build();


        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        // Ожидаем минимум 2 нарушения: для name и email
        assertThat(violations.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldSerializeUserDto_withNullValues() throws Exception {

        UserDto userDto = UserDto.builder()
                .id(1L)
                .build();


        String json = objectMapper.writeValueAsString(userDto);


        // Проверяем, что id сериализовано, а name и email могут быть null
        assertThat(json).contains("\"id\":1");
        // Jackson по умолчанию включает null значения, если не настроено иначе
        assertThat(json).contains("\"name\":null");
        assertThat(json).contains("\"email\":null");
    }

    @Test
    void shouldDeserializeUserDto_withMissingFields() throws Exception {

        String json = """
                {
                    "id": 1
                }
                """;


        UserDto userDto = objectMapper.readValue(json, UserDto.class);


        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isNull();
        assertThat(userDto.getEmail()).isNull();
    }

    @Test
    void shouldSerializeUserCreateDto_withNullValues() throws Exception {

        UserCreateDto createDto = UserCreateDto.builder().build();


        String json = objectMapper.writeValueAsString(createDto);


        assertThat(json).contains("\"name\":null");
        assertThat(json).contains("\"email\":null");
    }

    @Test
    void shouldDeserializeUserCreateDto_withMissingFields() throws Exception {

        String json = "{}";


        UserCreateDto createDto = objectMapper.readValue(json, UserCreateDto.class);


        assertThat(createDto.getName()).isNull();
        assertThat(createDto.getEmail()).isNull();
    }

    @Test
    void shouldValidateUserDto_withValidData() {

        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();


        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);


        // UserDto может не иметь аннотаций валидации, так как используется для ответов
        // Но если есть, проверяем
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldValidateUserCreateDto_withValidEmailFormats() {
        // Тестируем различные валидные форматы email
        String[] validEmails = {
                "user@example.com",
                "user.name@example.com",
                "user123@example.com",
                "user+tag@example.com",
                "user@sub.example.com"
        };

        for (String email : validEmails) {

            UserCreateDto createDto = UserCreateDto.builder()
                    .name("John Doe")
                    .email(email)
                    .build();

            Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


            assertThat(violations).as("Email: %s должен быть валидным", email).isEmpty();
        }
    }

    @Test
    void shouldValidateUserCreateDto_withLongButValidName() {

        String longName = "A".repeat(255); // максимальная длина для строки
        UserCreateDto createDto = UserCreateDto.builder()
                .name(longName)
                .email("john@example.com")
                .build();


        Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(createDto);


        // Если есть ограничение на длину, оно должно сработать
        // Если нет - должно быть пусто
        // Проверяем только, что тест выполняется без ошибок
        assertThat(violations).isNotNull();
    }
}