package ru.practicum.shareit.dto.item;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
}
