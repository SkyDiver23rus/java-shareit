package ru.practicum.shareit.booking.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookingDatesValidator.class)
@Documented
public @interface ValidBookingDates {
    String message() default "Дата окончания должна быть позже даты начала";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}