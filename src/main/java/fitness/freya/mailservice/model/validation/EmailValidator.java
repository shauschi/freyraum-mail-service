package fitness.freya.mailservice.model.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailValidator
    implements ConstraintValidator<ValidEmail, String> {

  static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@"
      + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";


  @Override
  public boolean isValid(final String email, final ConstraintValidatorContext context) {
    return email == null
        || "".equals(email)
        || validateEmail(email);
  }

  static boolean validateEmail(final String email) {
    final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    final Matcher matcher = pattern.matcher(email);
    return matcher.matches();
  }
}
