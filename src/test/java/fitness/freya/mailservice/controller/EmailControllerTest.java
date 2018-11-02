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
  public void shouldPassEmailEventToServiceAndReturnOk() throws Exception {
    // given
    final Map<String, String> params = new HashMap<>();
    params.put("first", "hello world");
    final CreateEmail event = new CreateEmail(
        "TEST_MAIL_TEMPLATE",
        params,
        Collections.singletonList("me@testmail.com"),
        Collections.emptyList(),
        Collections.emptyList()
    );
    final String json = mapper.writeValueAsString(event);

    // when + then
    mvc.perform(post("/emails")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Deine Mail wird verschickt."));
    verify(emailService).sendMail(event);
    verifyNoMoreInteractions(emailService);
  }


}