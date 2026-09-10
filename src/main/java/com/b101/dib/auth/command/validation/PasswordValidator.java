package com.b101.dib.auth.command.validation;

import com.b101.dib.auth.domain.PasswordPolicy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return PasswordPolicy.isValid(password);
    }
}
