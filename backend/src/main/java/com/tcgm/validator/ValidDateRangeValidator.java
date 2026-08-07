package com.tcgm.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, DateRange> {

    private String message;

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(DateRange dateRange, ConstraintValidatorContext context) {
        if (dateRange == null) {
            return true;
        }

        LocalDate start = dateRange.getStartDate();
        LocalDate end = dateRange.getEndDate();

        if (start == null || end == null) {
            return true;
        }

        if (start.isAfter(end)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                message.isEmpty() ? "La date de début doit être avant la date de fin" : message
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}