package tim6.agentservice.adapter.http.dto;

import static java.util.Objects.isNull;

import java.util.Date;
import java.util.Set;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tim6.agentservice.domain.model.Ad;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AdCreateDTO {

    @NotBlank(message = "Id can't be blank")
    private String id;

    @NotBlank(message = "Image url can't be blank")
    private String imageUrl;

    @NotBlank(message = "Ad url can't be blank")
    private String adUrl;

    @NotNull(message = "From date must be filled")
    private Date from;

    @NotNull(message = "To date must be filled")
    private Date to;

    @PositiveOrZero(message = "Daily limit must be positive or zero")
    private Integer dailyLimit;

    private Set<String> sexesToMatch;

    private Integer ageToMatchFrom;

    private Integer ageToMatchTo;

    private Set<String> tagsToMatch;

    public static Ad to(AdCreateDTO dto, String agentId) {
        Integer ageFromValue = dto.getAgeToMatchFrom();
        Integer ageToValue = dto.getAgeToMatchTo();

        if (isNull(ageFromValue) && isNull(ageToValue)) {
            ageFromValue = Integer.MAX_VALUE;
            ageToValue = Integer.MAX_VALUE;
        } else if (isNull(ageFromValue)) {
            ageFromValue = 0;
        } else if (isNull(ageToValue)) {
            ageToValue = Integer.MAX_VALUE;
        }
        return new Ad(dto.getId(), agentId, dto.getImageUrl(), dto.getAdUrl(), dto.getFrom(),
                dto.getTo(),
                dto.getDailyLimit(), 0, dto.getSexesToMatch(),
                ageFromValue,
                ageToValue, dto.getTagsToMatch());
    }

}
