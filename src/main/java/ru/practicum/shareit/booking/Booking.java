package ru.practicum.shareit.booking;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
    private String status;
}