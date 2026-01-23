package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.validation.ValidBookingDates;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidBookingDates
public class BookingCreateDto {

    @NotNull(message = "ID предмета обязателен для заполнения")
    private Long itemId;

    @NotNull(message = "Дата начала обязательна для заполнения")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания обязательна для заполнения")
    @Future(message = "Дата окончания должна быть в будущем")
    private LocalDateTime end;
}