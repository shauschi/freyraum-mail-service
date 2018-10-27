package fitness.freya.mailservice.service;

import fitness.freya.mailservice.exception.ResourceLoadingException;
import fitness.freya.mailservice.model.EmailTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceTest {

  @InjectMocks
  private TemplateService testee;

  @Mock
  private Environment environment;

  @Mock
  private ResourceService resourceService;

  @Before
  public void setUp() throws ResourceLoadingException {
    ReflectionTestUtils.setField(testee, "defaultSubject", "Default subject");
    when(environment.getProperty("mail.template.TEST_TEMPLATE")).thenReturn("Test subject");
    when(resourceService.getResourceAsString("TEST_TEMPLATE.html")).thenReturn("<html>Test template</html>");

    when(resourceService.getResourceAsString("NO_SUBJECT.html")).thenReturn("<html>Any other template</html>");
  }

  @Test
  public void shouldCreateEmailTemplateForExisitingTemplateId() throws ResourceLoadingException {
    // when
    final EmailTemplate result = testee.getTemplate("TEST_TEMPLATE");

    // then
    assertThat(result, notNullValue());
    assertThat(result.getSubject(), is("Test subject"));
    assertThat(result.getMessage(), containsString("Test template"));
  }

  @Test
  public void shouldUseDefaultSubject() throws ResourceLoadingException {
    // when
    final EmailTemplate result = testee.getTemplate("NO_SUBJECT");

    // then
    assertThat(result, notNullValue());
    assertThat(result.getSubject(), is("Default subject"));
    assertThat(result.getMessage(), containsString("Any other template"));
  }

  @Test
  public void shouldReturnMessageFromDefault() throws ResourceLoadingException {
    when(resourceService.getResourceAsString("OTHER_TEMPLATE.html"))
        .thenThrow(new ResourceLoadingException(""));
    when(resourceService.getResourceAsString("DEFAULT.html"))
        .thenReturn("<html>Default template</html>");

    // when
    final EmailTemplate result = testee.getTemplate("OTHER_TEMPLATE");

    // then
    verify(resourceService).getResourceAsString("OTHER_TEMPLATE.html");
    verify(resourceService).getResourceAsString("DEFAULT.html");
    assertThat(result, notNullValue());
    assertThat(result.getSubject(), is("Default subject"));
    assertThat(result.getMessage(), containsString("Default template"));
  }

  @Test(expected = ResourceLoadingException.class)
  public void shouldThrowExceptionWhenUnableToLoadDefaultTemplate() throws ResourceLoadingException {
    when(resourceService.getResourceAsString("OTHER_TEMPLATE.html"))
        .thenThrow(new ResourceLoadingException(""));
    when(resourceService.getResourceAsString("DEFAULT.html"))
        .thenThrow(new ResourceLoadingException(""));

    // when
    final EmailTemplate result = testee.getTemplate("OTHER_TEMPLATE");

    // then
    verify(resourceService).getResourceAsString("OTHER_TEMPLATE.html");
    verify(resourceService).getResourceAsString("DEFAULT.html");
  }

}