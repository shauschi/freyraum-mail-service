package fitness.freya.mailservice.model.validation;

import java.util.Collection;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailValidatorCollection
    implements ConstraintValidator<ValidEmail, Collection<String>> {

  @Override
  public boolean isValid(final Collection<String> email, final ConstraintValidatorContext context) {
    return email == null
        || email.isEmpty()
        || email.stream().allMatch(EmailValidator::validateEmail);
  }

}
