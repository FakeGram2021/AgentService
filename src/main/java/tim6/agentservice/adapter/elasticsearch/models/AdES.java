package tim6.agentservice.adapter.elasticsearch.models;

import java.util.Date;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import tim6.agentservice.domain.model.Ad;

@Document(indexName = "ads")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AdES {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String agentId;

    @Field(type = FieldType.Text)
    private String imageUrl;

    @Field(type = FieldType.Text)
    private String adUrl;

    @Field(name = "dateRange", type = FieldType.Date_Range)
    private DateRange dateRange;

    @Field(type = FieldType.Integer)
    private Integer dailyLimit;

    @Field(type = FieldType.Integer)
    private Integer placementsToday;

    @Field(type = FieldType.Keyword)
    private Set<String> sexesToMatch;

    @Field(name = "ageRange", type = FieldType.Integer_Range)
    private AgeRange ageRangeToMatch;

    @Field(type = FieldType.Keyword)
    private Set<String> tagsToMatch;

    public static AdES from(Ad ad) {
        return new AdES(
                ad.getId(), ad.getAgentId(), ad.getImageUrl(), ad.getAdUrl(),
                new DateRange(ad.getFrom(), ad.getTo()),
                ad.getDailyLimit(), ad.getPlacementsToday(), ad.getSexesToMatch(),
                new AgeRange(ad.getAgeToMatchFrom(),
                        ad.getAgeToMatchTo()), ad.getTagsToMatch()
        );
    }

    public static Ad to(AdES adES) {
        return new Ad(
                adES.getId(), adES.getAgentId(), adES.getImageUrl(), adES.getAdUrl(),
                adES.getDateRange().from, adES.getDateRange().to,
                adES.getDailyLimit(), adES.getPlacementsToday(), adES.getSexesToMatch(),
                adES.getAgeRangeToMatch().from, adES.getAgeRangeToMatch().from,
                adES.getTagsToMatch()
        );
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class DateRange {

        @Field(name = "gte")
        private Date from;
        @Field(name = "lte")
        private Date to;

    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class AgeRange {

        @Field(name = "gte")
        private Integer from;
        @Field(name = "lte")
        private Integer to;

    }

}
