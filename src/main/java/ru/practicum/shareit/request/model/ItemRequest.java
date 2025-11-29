package ru.practicum.shareit.request.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    private Long id;
    private String description;
    private Long requestorId;
    private LocalDateTime created;
}