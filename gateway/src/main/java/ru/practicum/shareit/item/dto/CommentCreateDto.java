package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateDto {

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 512, message = "Комментарий должен быть не длиннее 512 символов")
    private String text;
}