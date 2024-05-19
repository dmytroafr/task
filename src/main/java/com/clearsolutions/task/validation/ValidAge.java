package com.clearsolutions.task.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {AgeValidator.class})
public @interface ValidAge {
    String message() default "User must be at least {validAge} years old";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
