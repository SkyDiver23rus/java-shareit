package ru.practicum.shareit.booking.validation;

import ru.practicum.shareit.booking.dto.BookingCreateDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BookingDatesValidator implements ConstraintValidator<ValidBookingDates, BookingCreateDto> {

    @Override
    public boolean isValid(BookingCreateDto dto, ConstraintValidatorContext context) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            return true; // @NotNull will handle this
        }

        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Дата окончания должна быть позже даты начала")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}