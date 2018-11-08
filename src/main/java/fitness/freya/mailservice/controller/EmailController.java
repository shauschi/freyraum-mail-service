package fitness.freya.mailservice.controller;

import fitness.freya.mailservice.model.CreateEmail;
import fitness.freya.mailservice.model.MessageDto;
import fitness.freya.mailservice.service.EmailService;
import javax.mail.MessagingException;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
public class EmailController {

  private static final Logger LOGGER = LogManager.getLogger(EmailController.class);

  private final EmailService emailService;

  @Autowired
  public EmailController(final EmailService emailService) {
    this.emailService = emailService;
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<MessageDto> handleException(final Exception exception) {
    LOGGER.error("Error while sending message", exception.getMessage());
    return ResponseEntity
        .badRequest()
        .body(new MessageDto(exception.getMessage()));
  }

  @PostMapping
  public MessageDto createEmail(
      @RequestBody @Valid final CreateEmail createEmail) throws MessagingException {
    emailService.sendMail(createEmail);
    return new MessageDto("Deine Mail wird verschickt.");
  }

}
