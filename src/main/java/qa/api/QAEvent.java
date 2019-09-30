package qa.api;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class QAEvent {
    private String id;
    private String title;
    private String description;
    private String date;
}
