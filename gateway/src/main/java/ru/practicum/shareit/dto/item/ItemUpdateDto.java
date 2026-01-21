package ru.practicum.shareit.dto.item;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemUpdateDto {
    private String name;
    private String description;
    private Boolean available;
}
