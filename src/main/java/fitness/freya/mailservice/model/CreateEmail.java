package fitness.freya.mailservice.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmail {

  private String templateId;
  private Map<String, String> parameters;

  private List<String> to;
  private List<String> cc;
  private List<String> bcc;

}
