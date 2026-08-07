package com.tcgm.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidDateRangeValidator.class)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {

    String message() default "La date de début doit être avant la date de fin";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}