package fitness.freya.mailservice.service;

import fitness.freya.mailservice.exception.ResourceLoadingException;
import fitness.freya.mailservice.model.EmailTemplate;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {

  private static final Logger LOGGER = LogManager.getLogger(TemplateService.class);

  private final ResourceService resourceService;
  private final Environment environment;
  private final String defaultSubject;

  @Autowired
  public TemplateService(
      final ResourceService resourceService,
      final Environment environment,
      @Value("${mail.template.DEFAULT}") final String defaultSubject) {
    this.resourceService = resourceService;
    this.environment = environment;
    this.defaultSubject = defaultSubject;
  }

  public EmailTemplate getTemplate(final String templateId) throws ResourceLoadingException {
    final EmailTemplate template = new EmailTemplate();
    final String subject = getEmailSubject(templateId);
    template.setSubject(subject);
    final String message = getEmailMessage(templateId);
    template.setMessage(message);
    return template;
  }

  private String getEmailSubject(final String templateId) {
    return Optional.ofNullable(environment.getProperty("mail.template." + templateId))
        .orElse(defaultSubject);
  }

  private String getEmailMessage(final String templateId) throws ResourceLoadingException {
    try {
      return resourceService.getResourceAsString(templateId + ".html");
    } catch (final ResourceLoadingException e) {
      LOGGER.error("Template with id {} not found.", templateId);
    }
    return resourceService.getResourceAsString("DEFAULT.html");
  }

}
