package ru.practicum.shareit.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.dto.UserCreateDto;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.dto.UserUpdateDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Создание и получение пользователя - успешный сценарий")
    void addAndGetUser_success() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("Vlad")
                .email("vlad@test.com")
                .build();


        UserDto saved = userService.createUser(createDto);
        UserDto found = userService.getUserById(saved.getId());


        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Vlad");
        assertThat(found.getEmail()).isEqualTo("vlad@test.com");
    }

    @Test
    @DisplayName("Обновление пользователя - успешный сценарий")
    void updateUser_success() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("Vlad")
                .email("vlad@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Vladimir")
                .email("vladimir@test.com")
                .build();


        UserDto updated = userService.updateUser(saved.getId(), updateDto);


        assertThat(updated.getName()).isEqualTo("Vladimir");
        assertThat(updated.getEmail()).isEqualTo("vladimir@test.com");
        assertThat(updated.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ID")
    void getUserById_userNotFound() {

        Long nonExistentId = 999L;


        assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void getAllUsers_success() {

        UserCreateDto user1 = UserCreateDto.builder()
                .name("User1")
                .email("user1@test.com")
                .build();

        UserCreateDto user2 = UserCreateDto.builder()
                .name("User2")
                .email("user2@test.com")
                .build();

        userService.createUser(user1);
        userService.createUser(user2);


        List<UserDto> allUsers = userService.getAllUsers();


        assertThat(allUsers).hasSize(2);
        assertThat(allUsers)
                .extracting("email")
                .contains("user1@test.com", "user2@test.com");
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser_success() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("ToDelete")
                .email("delete@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);


        userService.deleteUser(saved.getId());


        assertThatThrownBy(() -> userService.getUserById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Обновление только имени пользователя")
    void updateUser_onlyName() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("Original")
                .email("original@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("UpdatedName")
                .build();


        UserDto updated = userService.updateUser(saved.getId(), updateDto);


        assertThat(updated.getName()).isEqualTo("UpdatedName");
        assertThat(updated.getEmail()).isEqualTo("original@test.com"); // email не изменился
    }

    @Test
    @DisplayName("Обновление только email пользователя")
    void updateUser_onlyEmail() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("Original")
                .email("original@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("updated@test.com")
                .build();


        UserDto updated = userService.updateUser(saved.getId(), updateDto);


        assertThat(updated.getName()).isEqualTo("Original"); // имя не изменилось
        assertThat(updated.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    @DisplayName("Создание пользователя с уже существующим email - должно выбрасывать исключение")
    void createUser_withDuplicateEmail_shouldThrowException() {

        UserCreateDto user1 = UserCreateDto.builder()
                .name("User1")
                .email("same@test.com")
                .build();

        UserCreateDto user2 = UserCreateDto.builder()
                .name("User2")
                .email("same@test.com") // дубликат
                .build();


        userService.createUser(user1);


        assertThatThrownBy(() -> userService.createUser(user2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email уже существует");
    }

    @Test
    @DisplayName("Обновление email на уже существующий - должно выбрасывать исключение")
    void updateUser_toExistingEmail_shouldThrowException() {

        // Создаем двух пользователей
        UserCreateDto user1 = UserCreateDto.builder()
                .name("User1")
                .email("user1@test.com")
                .build();

        UserCreateDto user2 = UserCreateDto.builder()
                .name("User2")
                .email("user2@test.com")
                .build();

        UserDto saved1 = userService.createUser(user1);
        UserDto saved2 = userService.createUser(user2);

        // Пытаемся обновить email второго пользователя на email первого
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("user1@test.com")
                .build();


        assertThatThrownBy(() -> userService.updateUser(saved2.getId(), updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email уже существует");
    }

    @Test
    @DisplayName("Обновление email на тот же самый - должно работать")
    void updateUser_toSameEmail_shouldWork() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("User")
                .email("user@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("user@test.com")
                .build();


        UserDto updated = userService.updateUser(saved.getId(), updateDto);


        assertThat(updated.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    @DisplayName("Обновление пользователя с пустым именем - должно выбрасывать исключение")
    void updateUser_withEmptyName_shouldThrowException() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("User")
                .email("user@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("")
                .build();


        assertThatThrownBy(() -> userService.updateUser(saved.getId(), updateDto))
                .isInstanceOf(RuntimeException.class) // В сервисе выбрасывается ValidationException
                .hasMessageContaining("Имя не может быть пустым");
    }

    @Test
    @DisplayName("Обновление пользователя с пустым email - должно выбрасывать исключение")
    void updateUser_withEmptyEmail_shouldThrowException() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("User")
                .email("user@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("")
                .build();


        assertThatThrownBy(() -> userService.updateUser(saved.getId(), updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email не может быть пустым");
    }

    @Test
    @DisplayName("Обновление пользователя с невалидным email форматом - должно выбрасывать исключение")
    void updateUser_withInvalidEmail_shouldThrowException() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("User")
                .email("user@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("invalid-email")
                .build();


        assertThatThrownBy(() -> userService.updateUser(saved.getId(), updateDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Некорректный формат email");
    }

    @Test
    @DisplayName("Получение пользователя после удаления - должно выбрасывать исключение")
    void getUser_afterDeletion_shouldThrowException() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("ToDelete")
                .email("delete@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);
        Long userId = saved.getId();


        userService.deleteUser(userId);


        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя - должно выбрасывать исключение")
    void deleteUser_notFound_shouldThrowException() {

        Long nonExistentId = 999L;


        assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                .isInstanceOfAny(
                        NotFoundException.class,
                        org.springframework.dao.EmptyResultDataAccessException.class
                );
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя - должно выбрасывать исключение")
    void updateUser_notFound_shouldThrowException() {

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated")
                .email("updated@test.com")
                .build();


        assertThatThrownBy(() -> userService.updateUser(999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными - проверка на уникальность через exists")
    void createUser_checkExistsAfterCreation() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("TestUser")
                .email("test@example.com")
                .build();


        UserDto saved = userService.createUser(createDto);
        boolean exists = userService.exists(saved.getId());


        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Проверка exists для несуществующего пользователя")
    void exists_forNonExistentUser_shouldReturnFalse() {

        boolean exists = userService.exists(999L);


        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Обновление с null значениями - должно оставлять старые значения")
    void updateUser_withNullValues_shouldKeepOldValues() {

        UserCreateDto createDto = UserCreateDto.builder()
                .name("OriginalName")
                .email("original@test.com")
                .build();
        UserDto saved = userService.createUser(createDto);

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name(null)
                .email(null)
                .build();


        UserDto updated = userService.updateUser(saved.getId(), updateDto);


        assertThat(updated.getName()).isEqualTo("OriginalName");
        assertThat(updated.getEmail()).isEqualTo("original@test.com");
    }
}