package fitness.freya.mailservice.controller;

import fitness.freya.mailservice.model.CreateEmailEvent;
import fitness.freya.mailservice.model.MessageDto;
import fitness.freya.mailservice.service.EmailService;
import javax.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
public class EmailController {

  private final EmailService emailService;

  @Autowired
  public EmailController(final EmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping("/create-email-event")
  public MessageDto createEmailEvent(@RequestBody final CreateEmailEvent createEmailEvent) throws MessagingException {
    emailService.sendMail(createEmailEvent);
    return new MessageDto("Deine Mail wird verschickt.");
  }

}
