package fitness.freya.mailservice.model.validation;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static fitness.freya.mailservice.model.validation.EmailValidator.EMAIL_PATTERN;

public class EmailValidatorCollection
    implements ConstraintValidator<ValidEmail, Collection<String>> {

  @Override
  public boolean isValid(final Collection<String> email, final ConstraintValidatorContext context) {
    return email.isEmpty()
        || email.stream().allMatch(EmailValidator::validateEmail);
  }

}
