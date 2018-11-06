package fitness.freya.mailservice.service;

import fitness.freya.mailservice.exception.ResourceLoadingException;
import fitness.freya.mailservice.model.CreateEmail;
import fitness.freya.mailservice.model.EmailTemplate;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private static final Logger LOGGER = LogManager.getLogger(EmailService.class);

  private final Properties props;
  private final boolean isDevelop;
  private final String developReceiver;
  private final String sender;
  private final Authenticator auth;
  private ResourceService resourceService;

  private final TemplateService templateService;

  @Autowired
  public EmailService(
      @Value("${DEVELOPMENT:true}") final boolean isDevelopment,
      @Value("${mail.develop.receiver}") final String developReceiver,
      @Value("${MAIL_HOST}") final String host,
      @Value("${MAIL_PORT}") final Integer port,
      @Value("${MAIL_USR}") final String username,
      @Value("${MAIL_PSW}") final String password,
      @Value("${mail.sender}") final String sender,
      final ResourceService resourceService,
      final TemplateService templateService) {
    this.isDevelop = isDevelopment;
    this.developReceiver = developReceiver;
    this.sender = sender;

    this.props = new Properties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", port);
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "false");

    this.auth = new Authenticator() {
      public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    };

    this.resourceService = resourceService;
    this.templateService = templateService;
  }

  public void sendMail(final CreateEmail createMail) throws MessagingException {
    final MimeMessage mail = getMimeMessage(createMail);
    sendMail(mail);
  }

  private MimeMessage getMimeMessage(final CreateEmail createMail) throws MessagingException {
    LOGGER.info("Create mime message for {} - {}", createMail.getTemplateId(), createMail.getParameters());
    final Session session = Session.getInstance(props, auth);
    final MimeMessage mail = new MimeMessage(session);
    mail.setFrom(new InternetAddress(sender));

    EmailTemplate template;
    try {
      template = templateService.getTemplate(createMail.getTemplateId());
      mail.setRecipients(Message.RecipientType.TO, toInternetAddress(createMail.getTo()));
      mail.setRecipients(Message.RecipientType.CC, toInternetAddress(createMail.getCc()));
      mail.setRecipients(Message.RecipientType.BCC, toInternetAddress(createMail.getBcc()));
    } catch (final ResourceLoadingException exception) {
      final String errorMessage = "Fehler beim Erstellen einer E-Mail";
      LOGGER.error(errorMessage, exception);
      mail.setRecipients(Message.RecipientType.TO, toInternetAddress(Collections.singletonList("admin@freya.fitness")));
      template = new EmailTemplate();
      template.setSubject(errorMessage);
      template.setMessage(String.format("Folgender Input konnte nicht verarbeitet werden:\n%s", createMail.toString()));
    }

    final String replacesMessage = resourceService.replacePlaceholder(template.getMessage(), createMail.getParameters());
    final MimeMultipart multipart = getMimeMultipart(replacesMessage);
    mail.setContent(multipart);

    final String replacedSubject = resourceService.replacePlaceholder(template.getSubject(), createMail.getParameters());
    mail.setSubject(replacedSubject);

    modifyForDevelopment(replacesMessage, mail);
    return mail;
  }

  private InternetAddress[] toInternetAddress(final List<String> list) throws AddressException {
    if (list == null) {
      return null;
    }

    final InternetAddress[] addresses = new InternetAddress[list.size()];
    for (int i = 0; i < list.size(); i++) {
      addresses[i] = new InternetAddress(list.get(i));
    }
    return addresses;
  }

  private void sendMail(final MimeMessage mail) throws MessagingException {
    try {
      Transport.send(mail);
    } catch (final MessagingException e) {
      LOGGER.error("Unable to send mail", e);
      if (!isDevelop) {
        throw e;
      } else {
        LOGGER.error("unable to send mail in development: \n{}\n{}\n\n{}",
            mail.getAllRecipients(),
            mail.getSubject(),
            getSaveMailContent(mail));
      }
    }
  }

  private String getSaveMailContent(final MimeMessage mail) {
    try {
      return mail.getContent().toString();
    } catch (final IOException | MessagingException e) {
      final String error = "unable to get content for mail";
      LOGGER.error(error, e);
      return error;
    }
  }

  private MimeMultipart getMimeMultipart(final String message) throws MessagingException {
    final MimeMultipart multipart = new MimeMultipart();
    final BodyPart messageBodyPart = new MimeBodyPart();
    messageBodyPart.setContent(message, "text/html; charset=utf-8");
    multipart.addBodyPart(messageBodyPart);

    final BodyPart freyRaumSvg = new MimeBodyPart();
    try {
      final File file = resourceService.getResourceAsFile("freyraum-white.png");
      final DataSource fds = new FileDataSource(file);
      freyRaumSvg.setDataHandler(new DataHandler(fds));
      freyRaumSvg.addHeader("Content-Type", "image/png");
      freyRaumSvg.addHeader("Content-ID", "<freyraum>");
      multipart.addBodyPart(freyRaumSvg);
    } catch (final ResourceLoadingException e) {
      LOGGER.error("Unable to load FreyRaum png for mail");
    }

    return multipart;
  }

  /**
   * Ensure that no mails are sent to any other account but the development email account. To see the original receiver,
   * they will be added to the message.
   *
   * @param email mail that will be send
   */
  private void modifyForDevelopment(final String message, final Message email) throws MessagingException {
    if (isDevelop) {
      final Address[] originalTo = email.getRecipients(Message.RecipientType.TO);
      final Address[] originalCc = email.getRecipients(Message.RecipientType.CC);
      final Address[] originalBcc = email.getRecipients(Message.RecipientType.BCC);
      final String additionalText =
          "<p>"
              + "This mail was send from a development environment and was ment to be send"
              + "</p>"
              + getReceiverBlock("to", originalTo)
              + getReceiverBlock("cc", originalCc)
              + getReceiverBlock("bcc", originalBcc)
              + "<p><b>--- ORIGINAL MESSAGE ---</b></p>";
      final String newMailText = message.replaceFirst(
          "<body>(.*)</body>", "<body>" + additionalText + "$1</body>");

      final InternetAddress[] addresses = {new InternetAddress(developReceiver)};
      email.setRecipients(Message.RecipientType.TO, addresses);
      email.setRecipients(Message.RecipientType.CC, null);
      email.setRecipients(Message.RecipientType.BCC, null);

      final MimeMultipart multipart = getMimeMultipart(newMailText);
      email.setContent(multipart);
    }
  }

  private String getReceiverBlock(final String block, final Address[] receiver) {
    if (null == receiver || receiver.length == 0) {
      return "";
    }
    final String receiverString = Arrays.stream(receiver)
        .map(Address::toString)
        .collect(Collectors.joining(", "));
    return "<p>" + block + ": " + receiverString + "</p>";
  }

}
