package fitness.freya.mailservice.model;

import fitness.freya.mailservice.model.validation.ValidEmail;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmail {

  @NotEmpty
  private String templateId;

  private Map<String, String> parameters;

  @NotEmpty
  @ValidEmail
  private List<String> to;

  @ValidEmail
  private List<String> cc;

  @ValidEmail
  private List<String> bcc;

}
