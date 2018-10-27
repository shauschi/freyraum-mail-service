package fitness.freya.mailservice.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class CreateEmailEvent {

  private String templateId;
  private Map<String, String> parameters;

  private List<String> to;
  private List<String> cc;
  private List<String> bcc;

}
