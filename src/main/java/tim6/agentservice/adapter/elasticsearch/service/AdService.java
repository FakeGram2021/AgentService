package tim6.agentservice.adapter.elasticsearch.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.ScriptQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import tim6.agentservice.adapter.elasticsearch.models.AdES;
import tim6.agentservice.adapter.elasticsearch.repository.AdRepository;
import tim6.agentservice.domain.exceptions.EntityNotFoundException;
import tim6.agentservice.domain.exceptions.InvalidParametersException;
import tim6.agentservice.domain.model.Ad;
import tim6.agentservice.domain.model.User;

@Service
public class AdService implements tim6.agentservice.domain.service.AdService {

    private static final String DAILY_LIMIT_NOT_HIT_SCRIPT =
            "doc[\"placementsToday\"].value < doc[\"dailyLimit\"].value";

    final AdRepository adRepository;
    final ElasticsearchOperations elasticsearchOperations;
    final UserService userService;


    @Autowired
    public AdService(AdRepository adRepository,
            ElasticsearchOperations elasticsearchOperations,
            UserService userService) {
        this.adRepository = adRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.userService = userService;
    }

    @Override
    public List<Ad> getAdsForUserId(String userId) {
        final Optional<User> optionalUser = userService.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException(String.format("User by id %s not found", userId));
        }
        final User user = optionalUser.get();

        NativeSearchQuery applicableAdsQuery = applicableAdsQuery(user);
        SearchHits<AdES> searchHits = elasticsearchOperations.search(applicableAdsQuery,
                AdES.class);

        List<Ad> ads = searchHits.stream()
                .map(SearchHit::getContent)
                .map(AdES::to)
                .filter(ad -> !ad.getAgentId().equals(userId))
                .collect(Collectors.toList());

        List<String> adIds = ads.stream().map(Ad::getId).collect(Collectors.toList());
        incrementDailyCounterForAdsById(adIds);
        return ads;
    }

    @Override
    public Optional<Ad> getAdById(String adId) {
        Optional<AdES> adES = adRepository.findById(adId);
        return adES.map(AdES::to);
    }

    @Override
    public void createAd(Ad ad) {
        if (!validateAdDates(ad)) {
            throw new InvalidParametersException("Ad dates are invalid");
        }

        if (!validateAgeFilter(ad)) {
            throw new InvalidParametersException("Ad age filters are invalid");
        }

        adRepository.save(AdES.from(ad));
    }

    @Override
    public void deleteAdById(String adId) {
        adRepository.deleteById(adId);
    }

    @Override
    public void incrementDailyCounterForAdsById(Collection<String> ids) {
        Iterable<AdES> ads = adRepository.findAllById(ids);
        ads.forEach(ad -> ad.setPlacementsToday(ad.getPlacementsToday() + 1));
        adRepository.saveAll(ads);
    }

    private NativeSearchQuery applicableAdsQuery(User user) {
        final QueryBuilder applicableAdsQueryBuilder = getApplicableAdsQueryBuilder(user);
        return new NativeSearchQueryBuilder().withQuery(applicableAdsQueryBuilder).build();
    }

    private BoolQueryBuilder getApplicableAdsQueryBuilder(User user) {
        final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(beApplicableToday());
        queryBuilder.must(notHitDailyLimit());
        queryBuilder.should(matchUsersSex(user.getSex()));
        queryBuilder.should(matchUsersAge(user.getBirthYear()));

        if (user.getPostedTagsHistory().size() > 0) {
            queryBuilder.should(matchUsersPostHistory(user.getPostedTagsHistory()));
        }
        queryBuilder.minimumShouldMatch(1);

        return queryBuilder;
    }

    private TermQueryBuilder beApplicableToday() {
        return QueryBuilders.termQuery("dateRange", new Date());
    }

    private ScriptQueryBuilder notHitDailyLimit() {
        return QueryBuilders.scriptQuery(
                new Script(ScriptType.INLINE, "painless", DAILY_LIMIT_NOT_HIT_SCRIPT,
                        new HashMap<>()));
    }

    private MatchQueryBuilder matchUsersSex(String sex) {
        return QueryBuilders.matchQuery("sexesToMatch", sex);
    }

    private TermQueryBuilder matchUsersAge(Integer birthYear) {
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return QueryBuilders.termQuery("ageRange", currentYear - birthYear);
    }

    private BoolQueryBuilder matchUsersPostHistory(List<String> tags) {
        final BoolQueryBuilder tagsBoolQuery = QueryBuilders.boolQuery();
        tags.forEach(tag ->
                tagsBoolQuery.should(QueryBuilders.matchQuery("tagsToMatch", tag))
        );
        return tagsBoolQuery;
    }

    private boolean validateAdDates(Ad ad) {
        return ad.getTo().after(ad.getFrom());
    }

    private boolean validateAgeFilter(Ad ad) {
        Integer from = ad.getAgeToMatchFrom();
        Integer to = ad.getAgeToMatchTo();

        if (isNull(from) && nonNull(to)) {
            return false;
        }

        if (nonNull(from) && isNull(to)) {
            return false;
        }

        if (nonNull(from) && nonNull(to)) {
            return ad.getAgeToMatchTo() >= ad.getAgeToMatchFrom();
        } else {
            return true;
        }
    }

}
