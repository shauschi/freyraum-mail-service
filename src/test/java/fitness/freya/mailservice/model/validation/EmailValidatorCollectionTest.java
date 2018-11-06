package fitness.freya.mailservice.model.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class EmailValidatorCollectionTest {

  private class Testee {
    @ValidEmail
    private List<String> email;

    Testee(final List<String> email) {
      this.email = email;
    }
  }

  private Validator validator;

  @Before
  public void setUp() {
    ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    this.validator = validatorFactory.getValidator();
  }

  @Test
  public void shouldFailOnInvalidEmail() {
    final Testee testee = new Testee(Collections.singletonList("invalidmail"));

    final Set<ConstraintViolation<Testee>> violations = validator.validate(testee);

    assertThat(violations.size(), equalTo(1));
    final ConstraintViolation<Testee> violation = violations.iterator().next();
    assertThat(violation.getMessage(), equalTo("Invalid email"));
  }

  @Test
  public void shouldFailOnAnyInvalidEmail() {
    final Testee testee = new Testee(Arrays.asList("any@valid.de", "invalidmail"));

    final Set<ConstraintViolation<Testee>> violations = validator.validate(testee);

    assertThat(violations.size(), equalTo(1));
    final ConstraintViolation<Testee> violation = violations.iterator().next();
    assertThat(violation.getMessage(), equalTo("Invalid email"));
  }

  @Test
  public void shouldAcceptValidMail() {
    final Testee testee = new Testee(Collections.singletonList("valid@mail.de"));

    final Set<ConstraintViolation<Testee>> violations = validator.validate(testee);

    assertThat(violations.size(), equalTo(0));
  }

  @Test
  public void shouldAcceptValidMails() {
    final Testee testee = new Testee(Arrays.asList("valid@mail.de", "another@mail.de"));

    final Set<ConstraintViolation<Testee>> violations = validator.validate(testee);

    assertThat(violations.size(), equalTo(0));
  }

}