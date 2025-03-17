package com.winseslas.microservices.bookstore.UserManager.validation;

import com.winseslas.microservices.bookstore.UserManager.model.entity.BPartnerGroup;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OneMandatoryFieldValidator implements ConstraintValidator<OneMandatoryField, BPartnerGroup> {

    @Override
    public boolean isValid(BPartnerGroup bPartnerGroup, ConstraintValidatorContext context) {
        int count = 0;
        if (Boolean.TRUE.equals(bPartnerGroup.getIsCustomer())) count++;
        if (Boolean.TRUE.equals(bPartnerGroup.getIsAuthor())) count++;
        if (Boolean.TRUE.equals(bPartnerGroup.getIsEmployee())) count++;

        return count == 1;
    }
}