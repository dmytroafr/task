package com.clearsolutions.task.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {

    @Value("${request.age}")
    private int validAge;

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true;
        }
        int age = LocalDate.now().getYear() - birthDate.getYear();
        return age >= validAge;
    }
}
