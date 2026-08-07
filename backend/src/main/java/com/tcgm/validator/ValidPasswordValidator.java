package com.tcgm.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSWORD_PATTERN = 
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    private String message;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Le mot de passe est obligatoire")
                .addConstraintViolation();
            return false;
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Le mot de passe doit contenir au moins 8 caractères, une majuscule, " +
                "une minuscule, un chiffre et un caractère spécial (@#$%^&+=!)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}