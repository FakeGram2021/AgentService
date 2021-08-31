package tim6.agentservice.ads.integration;

import static org.junit.Assert.assertEquals;
import static tim6.agentservice.helpers.AuthHelper.createAuthToken;

import com.github.javafaker.Faker;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import tim6.agentservice.adapter.elasticsearch.models.AdES;
import tim6.agentservice.adapter.elasticsearch.models.UserES;
import tim6.agentservice.adapter.elasticsearch.repository.AdRepository;
import tim6.agentservice.adapter.elasticsearch.repository.UserRepository;
import tim6.agentservice.adapter.http.dto.AdGetDTO;
import tim6.agentservice.common.CommonTestBase;
import tim6.agentservice.domain.model.Ad;
import tim6.agentservice.domain.model.User;

public class GetAdsTest extends CommonTestBase {

    private static final Faker faker = Faker.instance();
    private final String AGENT_ID = UUID.randomUUID().toString();
    private final String MALE_USER_ID = UUID.randomUUID().toString();
    private final String TEENAGE_USER_ID = UUID.randomUUID().toString();
    private final String HISTORIAN_USER_ID = UUID.randomUUID().toString();

    private final String MALE_TARGET_AD_ID = UUID.randomUUID().toString();
    private final String TEENAGE_TARGET_AD_ID = UUID.randomUUID().toString();
    private final String HISTORY_TARGET_AD_ID = UUID.randomUUID().toString();

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdRepository adRepository;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @LocalServerPort
    private int port;

    @Before
    public void initializeData() {
        initializeUsers();
        initializePosts();
    }

    @After
    public void cleanUpData() {
        userRepository.deleteAll();
        adRepository.deleteAll();
    }

    private void initializeUsers() {
        final User agent = new User(AGENT_ID, "MALE", 1997, List.of("idk"));
        final UserES agentES = UserES.from(agent);
        userRepository.save(agentES);

        final User maleUser = new User(MALE_USER_ID, "MALE", 1997, new ArrayList<>());
        final UserES maleUserES = UserES.from(maleUser);
        userRepository.save(maleUserES);

        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final User teenageUser = new User(TEENAGE_USER_ID, "UNSPECIFIED", currentYear - 15,
                new ArrayList<>());
        final UserES teenageUserES = UserES.from(teenageUser);
        userRepository.save(teenageUserES);

        final User matchingHistoryUser = new User(HISTORIAN_USER_ID, "UNSPECIFIED", 1997,
                List.of("history", "historical"));
        final UserES matchingHistoryUserES = UserES.from(matchingHistoryUser);
        userRepository.save(matchingHistoryUserES);
    }

    private void initializePosts() {
        final Ad maleTargetAd = new Ad(MALE_TARGET_AD_ID, AGENT_ID, "imageUrl", "adUrl",
                faker.date().past(1, TimeUnit.DAYS), faker.date().future(1, TimeUnit.DAYS), 1, 0,
                Set.of("MALE"), Integer.MAX_VALUE, Integer.MAX_VALUE, null);
        final AdES maleTargetAdES = AdES.from(maleTargetAd);
        adRepository.save(maleTargetAdES);

        final Ad teenageTargetAd = new Ad(TEENAGE_TARGET_AD_ID, AGENT_ID, "imageUrl", "adUrl",
                faker.date().past(1, TimeUnit.DAYS), faker.date().future(1, TimeUnit.DAYS), 1, 0,
                null, 13, 19, null);
        final AdES teenageTargetAdES = AdES.from(teenageTargetAd);
        adRepository.save(teenageTargetAdES);

        final Ad historyTargetAd = new Ad(HISTORY_TARGET_AD_ID, AGENT_ID, "imageUrl", "adUrl",
                faker.date().past(1, TimeUnit.DAYS), faker.date().future(1, TimeUnit.DAYS), 1, 0,
                null, Integer.MAX_VALUE, Integer.MAX_VALUE, Set.of("history"));
        final AdES historyTargetAdES = AdES.from(historyTargetAd);
        adRepository.save(historyTargetAdES);
    }

    @Test
    public void testAdMatchingSex() {
        URI adsUri = buildGetAds();
        ResponseEntity<List<AdGetDTO>> response = sendGetPost(adsUri,
                createAuthToken(MALE_USER_ID, JWT_SECRET));
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<AdGetDTO> ads = response.getBody();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ads).isNotNull();
        softly.assertThat(ads.size()).isEqualTo(1);
        softly.assertThat(ads.get(0).getId()).isEqualTo(MALE_TARGET_AD_ID);
        softly.assertAll();
    }

    @Test
    public void testAdMatchingAge() {
        URI adsUri = buildGetAds();
        ResponseEntity<List<AdGetDTO>> response = sendGetPost(adsUri,
                createAuthToken(TEENAGE_USER_ID, JWT_SECRET));
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<AdGetDTO> ads = response.getBody();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ads).isNotNull();
        softly.assertThat(ads.size()).isEqualTo(1);
        softly.assertThat(ads.get(0).getId()).isEqualTo(TEENAGE_TARGET_AD_ID);
        softly.assertAll();
    }

    @Test
    public void testAdMatchingInterests() {
        URI adsUri = buildGetAds();
        ResponseEntity<List<AdGetDTO>> response = sendGetPost(adsUri,
                createAuthToken(HISTORIAN_USER_ID, JWT_SECRET));
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<AdGetDTO> ads = response.getBody();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ads).isNotNull();
        softly.assertThat(ads.size()).isEqualTo(1);
        softly.assertThat(ads.get(0).getId()).isEqualTo(HISTORY_TARGET_AD_ID);
        softly.assertAll();
    }

    @Test
    public void testAdNotShowingUpWhenOverLimit() {
        URI adsUri = buildGetAds();
        ResponseEntity<List<AdGetDTO>> response = sendGetPost(adsUri,
                createAuthToken(HISTORIAN_USER_ID, JWT_SECRET));
        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<AdGetDTO> ads = response.getBody();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ads).isNotNull();
        softly.assertThat(ads.size()).isEqualTo(1);
        softly.assertThat(ads.get(0).getId()).isEqualTo(HISTORY_TARGET_AD_ID);

        ResponseEntity<List<AdGetDTO>> response2 = sendGetPost(adsUri,
                createAuthToken(HISTORIAN_USER_ID, JWT_SECRET));
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        List<AdGetDTO> ads2 = response2.getBody();
        softly.assertThat(ads2).isNotNull();
        softly.assertThat(ads2.size()).isEqualTo(0);

        softly.assertAll();
    }

    private URI buildGetAds() {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format("http://localhost:%d/api/v1/ads", this.port));
        return builder.build().encode().toUri();
    }

    private ResponseEntity<List<AdGetDTO>> sendGetPost(final URI uri, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);
        return this.testRestTemplate.exchange(
                uri, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                });
    }

    private ResponseEntity<List<AdGetDTO>> sendGetPost(final URI uri) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_LENGTH, "0");
        final HttpEntity<Void> entity = new HttpEntity<>(null, headers);
        return this.testRestTemplate.exchange(
                uri, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
                });
    }

}
