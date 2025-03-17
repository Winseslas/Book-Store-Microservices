package com.winseslas.microservices.bookstore.UserManager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
@Documented
public @interface ValidPhone {
    String message() default "Format de numéro de téléphone invalide";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
