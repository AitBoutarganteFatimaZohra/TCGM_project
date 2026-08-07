package com.tcgm.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {

    String message() default "Cet email est déjà utilisé";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}