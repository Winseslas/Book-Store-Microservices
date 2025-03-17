package com.winseslas.microservices.bookstore.UserManager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    private static final String PHONE_PATTERN = 
        "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        return phone != null && Pattern.compile(PHONE_PATTERN).matcher(phone).matches();
    }
}
