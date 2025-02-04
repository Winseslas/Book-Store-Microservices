package com.winseslas.microservices.bookStore.UserManager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = OneMandatoryFieldValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneMandatoryField {
    String message() default "Exactly one of IsCustomer, IsAuthor, or isEmployee must be true.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
