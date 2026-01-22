package ru.practicum.shareit.server.user.dto;

import jakarta.validation.constraints.Email;
import lombok.*;

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