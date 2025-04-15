package ru.practicum.event.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventDateValidator.class)
public @interface EventDate {
    String message() default "должно содержать дату, которая еще не наступила";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
