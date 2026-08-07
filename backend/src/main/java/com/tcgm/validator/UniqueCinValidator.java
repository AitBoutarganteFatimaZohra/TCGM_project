package com.tcgm.validator;

import com.tcgm.repository.OuvrierRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueCinValidator implements ConstraintValidator<UniqueCin, String> {

    private final OuvrierRepository ouvrierRepository;

    @Override
    public boolean isValid(String cin, ConstraintValidatorContext context) {
        if (cin == null || cin.isEmpty()) {
            return true;
        }
        return !ouvrierRepository.existsByCin(cin);
    }
}