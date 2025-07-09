package org.retrade.main.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.retrade.main.model.annotation.PasswordMatches;
import org.retrade.main.model.dto.request.ResetPasswordRequest;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, ResetPasswordRequest> {
    @Override
    public boolean isValid(ResetPasswordRequest request, ConstraintValidatorContext constraintValidatorContext) {
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null) {
            return true;
        }
        return request.getNewPassword().equals(request.getConfirmNewPassword());
    }
}
