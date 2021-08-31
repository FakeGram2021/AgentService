package tim6.agentservice.adapter.kafka.models.payloads;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserPayload {

    private String id;

    private String sex;

    private Integer birthYear;

}
