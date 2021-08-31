package tim6.agentservice.domain.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import tim6.agentservice.domain.model.Ad;

public interface AdService {

    List<Ad> getAdsForUserId(final String userId);

    Optional<Ad> getAdById(final String adId);

    void createAd(final Ad ad);

    void deleteAdById(final String adId);

    void incrementDailyCounterForAdsById(final Collection<String> id);
}
