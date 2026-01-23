package ru.practicum.shareit.request.dto;

import javax.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestCreateDto {
    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
}