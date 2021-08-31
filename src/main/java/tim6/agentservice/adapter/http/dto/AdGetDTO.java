package tim6.agentservice.adapter.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tim6.agentservice.domain.model.Ad;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AdGetDTO {

    private String id;

    private String imageUrl;

    private String adUrl;

    public static AdGetDTO from(Ad ad) {
        return new AdGetDTO(ad.getId(), ad.getImageUrl(), ad.getAdUrl());
    }

}
