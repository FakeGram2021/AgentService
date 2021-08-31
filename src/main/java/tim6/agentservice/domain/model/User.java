package tim6.agentservice.domain.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class User {

    private String id;

    private String sex;

    private Integer birthYear;

    private List<String> postedTagsHistory;

}
