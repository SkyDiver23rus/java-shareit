package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 255, message = "Название должно быть не длиннее 255 символов")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 512, message = "Описание должно быть не длиннее 512 символов")
    private String description;

    @NotNull(message = "Поле 'available' обязательно для заполнения")
    private Boolean available;

    private Long requestId;
}