package org.retrade.main.model.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.retrade.main.validator.PasswordMatchesValidator;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {
    String message() default "New password and confirm password do not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
