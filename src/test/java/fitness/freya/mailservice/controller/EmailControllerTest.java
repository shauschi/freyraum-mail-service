package fitness.freya.mailservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fitness.freya.mailservice.model.CreateEmail;
import fitness.freya.mailservice.service.EmailService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EmailController.class)
public class EmailControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper mapper;

  @MockBean
  private EmailService emailService;

  @Test
  public void shouldPassEmailToServiceAndReturnOk() throws Exception {
    // given
    final CreateEmail email = givenCreateEmail();
    final String json = mapper.writeValueAsString(email);

    // when + then
    mvc.perform(post("/emails")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Deine Mail wird verschickt."));
    verify(emailService).sendMail(email);
    verifyNoMoreInteractions(emailService);
  }

  @Test
  public void shouldReturnErrorMessageIfServiceFails() throws Exception {
    // given
    final CreateEmail email = givenCreateEmail();
    final String json = mapper.writeValueAsString(email);
    doThrow(new RuntimeException("Any error"))
        .when(emailService).sendMail(email);

    // when + then
    mvc.perform(post("/emails")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Any error"));
    verify(emailService).sendMail(email);
    verifyNoMoreInteractions(emailService);
  }

  @Test
  public void shouldNotAcceptInvalidEmail() throws Exception {
    // given
    final CreateEmail email = givenCreateEmail();
    email.setTo(Collections.singletonList("invalid@to"));
    final String json = mapper.writeValueAsString(email);

    // when + then
    mvc.perform(post("/emails")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("Validation failed")));
    verifyNoMoreInteractions(emailService);
  }

  private CreateEmail givenCreateEmail() {
    final Map<String, String> params = new HashMap<>();
    params.put("first", "hello world");
    return new CreateEmail(
        "TEST_MAIL_TEMPLATE",
        params,
        Collections.singletonList("me@testmail.com"),
        Collections.emptyList(),
        Collections.emptyList()
    );
  }

}