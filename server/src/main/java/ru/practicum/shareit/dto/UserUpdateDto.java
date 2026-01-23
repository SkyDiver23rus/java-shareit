package ru.practicum.shareit.dto;

import javax.validation.constraints.Email;
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