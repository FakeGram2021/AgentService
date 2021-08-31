package tim6.agentservice.domain.model;

import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Ad {

    private String id;

    private String agentId;

    private String imageUrl;

    private String adUrl;

    private Date from;

    private Date to;

    private Integer dailyLimit;

    private Integer placementsToday;

    private Set<String> sexesToMatch;

    private Integer ageToMatchFrom;

    private Integer ageToMatchTo;

    private Set<String> tagsToMatch;

}
