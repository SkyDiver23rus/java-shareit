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
import ru.practicum.shareit.dto.UserCreateDto;
import ru.practicum.shareit.dto.UserDto;
import ru.practicum.shareit.dto.UserUpdateDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto testUserDto;
    private UserCreateDto testUserCreateDto;
    private UserUpdateDto testUserUpdateDto;

    @BeforeEach
    void setUp() {
        testUserDto = UserDto.builder()
                .id(1L)
                .name("Vlad")
                .email("vlad@test.com")
                .build();

        testUserCreateDto = UserCreateDto.builder()
                .name("Vlad")
                .email("vlad@test.com")
                .build();

        testUserUpdateDto = UserUpdateDto.builder()
                .name("Updated Vlad")
                .email("updated@test.com")
                .build();
    }

    @Test
    @DisplayName("GET /users/{id} - успешное получение пользователя")
    void getUserById_shouldReturnUser_whenUserExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUserDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Vlad"))
                .andExpect(jsonPath("$.email").value("vlad@test.com"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    @DisplayName("GET /users/{id} - пользователь не найден")
    void getUserById_shouldReturnNotFound_whenUserNotExists() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    @DisplayName("GET /users/{id} - с некорректным ID (не число)")
    void getUserById_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(get("/users/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users - успешное создание пользователя")
    void createUser_shouldReturnCreated_whenValidData() throws Exception {
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(testUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Vlad"))
                .andExpect(jsonPath("$.email").value("vlad@test.com"));

        verify(userService, times(1)).createUser(any(UserCreateDto.class));
    }

    @Test
    @DisplayName("POST /users - дублирование email (409 CONFLICT)")
    void createUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new IllegalStateException("Email уже существует"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserCreateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email уже существует"));

        verify(userService, times(1)).createUser(any(UserCreateDto.class));
    }

    @Test
    @DisplayName("POST /users - с пустым телом запроса")
    void createUser_shouldReturnBadRequest_whenEmptyBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users - без email")
    void createUser_shouldReturnBadRequest_whenEmailMissing() throws Exception {
        UserCreateDto dto = UserCreateDto.builder()
                .name("Vlad")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users - без имени")
    void createUser_shouldReturnBadRequest_whenNameMissing() throws Exception {
        UserCreateDto dto = UserCreateDto.builder()
                .email("vlad@test.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/{id} - успешное обновление")
    void updateUser_shouldReturnOk_whenValidUpdate() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("Updated Vlad")
                .email("updated@test.com")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Vlad"))
                .andExpect(jsonPath("$.email").value("updated@test.com"));

        verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("PATCH /users/{id} - обновление только имени")
    void updateUser_shouldReturnOk_whenOnlyNameUpdated() throws Exception {
        UserUpdateDto partialUpdate = UserUpdateDto.builder()
                .name("New Name Only")
                .build();

        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("New Name Only")
                .email("vlad@test.com")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name Only"))
                .andExpect(jsonPath("$.email").value("vlad@test.com"));

        verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("PATCH /users/{id} - пользователь не найден")
    void updateUser_shouldReturnNotFound_whenUserNotExists() throws Exception {
        when(userService.updateUser(eq(999L), any(UserUpdateDto.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));

        verify(userService, times(1)).updateUser(eq(999L), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("PATCH /users/{id} - невалидный email")
    void updateUser_shouldReturnBadRequest_whenInvalidEmail() throws Exception {
        UserUpdateDto invalidEmailDto = UserUpdateDto.builder()
                .email("not-an-email")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class)))
                .thenThrow(new ValidationException("Некорректный формат email"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("PATCH /users/{id} - конфликт email при обновлении (409 CONFLICT)")
    void updateUser_shouldReturnConflict_whenEmailConflict() throws Exception {
        when(userService.updateUser(eq(1L), any(UserUpdateDto.class)))
                .thenThrow(new IllegalStateException("Email уже существует"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email уже существует"));

        verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("DELETE /users/{id} - успешное удаление")
    void deleteUser_shouldReturnOk_whenUserExists() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /users/{id} - удаление несуществующего пользователя")
    void deleteUser_shouldReturnOk_whenUserNotExists() throws Exception {
        doNothing().when(userService).deleteUser(999L);

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(999L);
    }

    @Test
    @DisplayName("GET /users - получение всех пользователей")
    void getAllUsers_shouldReturnList_whenUsersExist() throws Exception {
        List<UserDto> users = List.of(
                UserDto.builder().id(1L).name("User1").email("user1@test.com").build(),
                UserDto.builder().id(2L).name("User2").email("user2@test.com").build()
        );

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("User2"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /users - пустой список")
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("POST /users - внутренняя ошибка сервера")
    void createUser_shouldReturnInternalError_whenServiceThrowsRuntimeException() throws Exception {
        when(userService.createUser(any(UserCreateDto.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserCreateDto)))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).createUser(any(UserCreateDto.class));
    }

    @Test
    @DisplayName("POST /users - с null значениями")
    void createUser_shouldReturnBadRequest_whenNullValues() throws Exception {
        UserCreateDto dto = UserCreateDto.builder()
                .name(null)
                .email(null)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/{id} - пустое имя")
    void updateUser_shouldReturnBadRequest_whenEmptyName() throws Exception {
        UserUpdateDto emptyNameDto = UserUpdateDto.builder()
                .name("")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class)))
                .thenThrow(new ValidationException("Имя не может быть пустым"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyNameDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("PATCH /users/{id} - пустой email")
    void updateUser_shouldReturnBadRequest_whenEmptyEmail() throws Exception {
        UserUpdateDto emptyEmailDto = UserUpdateDto.builder()
                .email("")
                .build();

        when(userService.updateUser(eq(1L), any(UserUpdateDto.class)))
                .thenThrow(new ValidationException("Email не может быть пустым"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyEmailDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).updateUser(eq(1L), any(UserUpdateDto.class));
    }

    @Test
    @DisplayName("POST /users - слишком длинные поля")
    void createUser_shouldReturnBadRequest_whenFieldsTooLong() throws Exception {
        String tooLongName = "A".repeat(256); // больше 255 символов
        String tooLongEmail = "a".repeat(256) + "@test.com";

        UserCreateDto dto = UserCreateDto.builder()
                .name(tooLongName)
                .email(tooLongEmail)
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /users/{id} - с некорректным ID (не число)")
    void updateUser_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(patch("/users/abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /users/{id} - с некорректным ID (не число)")
    void deleteUser_shouldReturnBadRequest_whenInvalidId() throws Exception {
        mockMvc.perform(delete("/users/abc"))
                .andExpect(status().isBadRequest());
    }
}