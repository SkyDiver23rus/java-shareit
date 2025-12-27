package ru.practicum.shareit.user.dto;

import lombok.*;

import jakarta.validation.constraints.Email;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    private String name;

    @Email(message = "Введите корректный Email")
    private String email;
}