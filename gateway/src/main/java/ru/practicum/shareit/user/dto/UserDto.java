package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.constraints.Email;
import jakarta.validation.constraints.constraints.NotBlank;
import jakarta.validation.constraints.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 255, message = "Имя должно быть не длиннее 255 символов")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Введите корректный Email")
    @Size(max = 512, message = "Email должен быть не длиннее 512 символов")
    private String email;
}