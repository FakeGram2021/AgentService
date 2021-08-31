package tim6.agentservice.ads.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static tim6.agentservice.helpers.AuthHelper.createAuthToken;

import com.github.javafaker.Faker;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;
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
import tim6.agentservice.adapter.http.dto.AdCreateDTO;
import tim6.agentservice.common.CommonTestBase;
import tim6.agentservice.domain.model.User;

public class CreateAdTest extends CommonTestBase {

    private static final Faker faker = Faker.instance();
    private final String AGENT_ID = UUID.randomUUID().toString();
    private final String AD_ID = UUID.randomUUID().toString();
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
    }

    @After
    public void cleanUpData() {
        userRepository.deleteAll();
        adRepository.deleteAll();
    }

    private void initializeUsers() {
        final User agent = new User(AGENT_ID, "MALE", 1997, new ArrayList<>());
        final UserES agentES = UserES.from(agent);
        userRepository.save(agentES);
    }

    @Test
    public void testCreateAdMatchingSex() {
        final AdCreateDTO adCreateDTO = new AdCreateDTO(
                AD_ID,
                "imageUrl",
                "adUrl",
                faker.date().future(5, TimeUnit.DAYS),
                faker.date().future(6, 5, TimeUnit.DAYS),
                5,
                Set.of("MALE", "FEMALE"),
                null,
                null, null
        );

        final URI adCreateURI = this.thisBuildAdCreateURI();
        final ResponseEntity<Void> response = this.sendPostRequest(adCreateURI, adCreateDTO,
                createAuthToken(this.AGENT_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Optional<AdES> optionalAdES = adRepository.findById(AD_ID);
        assertTrue(optionalAdES.isPresent());

        AdES adEs = optionalAdES.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(adEs.getId()).isEqualTo(AD_ID);
        softly.assertThat(adEs.getImageUrl()).isEqualTo(adCreateDTO.getImageUrl());
        softly.assertThat(adEs.getAdUrl()).isEqualTo(adCreateDTO.getAdUrl());
        softly.assertThat(adEs.getDateRange().getFrom()).isEqualTo(adCreateDTO.getFrom());
        softly.assertThat(adEs.getDateRange().getTo()).isEqualTo(adCreateDTO.getTo());
        softly.assertThat(adEs.getDailyLimit()).isEqualTo(adCreateDTO.getDailyLimit());
        softly.assertThat(adEs.getSexesToMatch()).isEqualTo(adCreateDTO.getSexesToMatch());
        softly.assertThat(adEs.getAgeRangeToMatch().getFrom())
                .isEqualTo(Integer.MAX_VALUE);
        softly.assertThat(adEs.getAgeRangeToMatch().getTo())
                .isEqualTo(Integer.MAX_VALUE);
        softly.assertThat(adEs.getTagsToMatch()).isEqualTo(adCreateDTO.getTagsToMatch());
        softly.assertAll();
    }

    @Test
    public void testCreateAdMatchingAge() {
        final AdCreateDTO adCreateDTO = new AdCreateDTO(
                AD_ID,
                "imageUrl",
                "adUrl",
                faker.date().future(5, TimeUnit.DAYS),
                faker.date().future(6, 5, TimeUnit.DAYS),
                5,
                null,
                20,
                30, null
        );

        final URI adCreateURI = this.thisBuildAdCreateURI();
        final ResponseEntity<Void> response = this.sendPostRequest(adCreateURI, adCreateDTO,
                createAuthToken(this.AGENT_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Optional<AdES> optionalAdES = adRepository.findById(AD_ID);
        assertTrue(optionalAdES.isPresent());

        AdES adEs = optionalAdES.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(adEs.getId()).isEqualTo(AD_ID);
        softly.assertThat(adEs.getImageUrl()).isEqualTo(adCreateDTO.getImageUrl());
        softly.assertThat(adEs.getAdUrl()).isEqualTo(adCreateDTO.getAdUrl());
        softly.assertThat(adEs.getDateRange().getFrom()).isEqualTo(adCreateDTO.getFrom());
        softly.assertThat(adEs.getDateRange().getTo()).isEqualTo(adCreateDTO.getTo());
        softly.assertThat(adEs.getDailyLimit()).isEqualTo(adCreateDTO.getDailyLimit());
        softly.assertThat(adEs.getSexesToMatch()).isEqualTo(adCreateDTO.getSexesToMatch());
        softly.assertThat(adEs.getAgeRangeToMatch().getFrom())
                .isEqualTo(20);
        softly.assertThat(adEs.getAgeRangeToMatch().getTo())
                .isEqualTo(30);
        softly.assertThat(adEs.getTagsToMatch()).isEqualTo(adCreateDTO.getTagsToMatch());
        softly.assertAll();
    }

    @Test
    public void testCreateAdMatchingAgeLower() {
        final AdCreateDTO adCreateDTO = new AdCreateDTO(
                AD_ID,
                "imageUrl",
                "adUrl",
                faker.date().future(5, TimeUnit.DAYS),
                faker.date().future(6, 5, TimeUnit.DAYS),
                5,
                null,
                20,
                null, null
        );

        final URI adCreateURI = this.thisBuildAdCreateURI();
        final ResponseEntity<Void> response = this.sendPostRequest(adCreateURI, adCreateDTO,
                createAuthToken(this.AGENT_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Optional<AdES> optionalAdES = adRepository.findById(AD_ID);
        assertTrue(optionalAdES.isPresent());

        AdES adEs = optionalAdES.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(adEs.getId()).isEqualTo(AD_ID);
        softly.assertThat(adEs.getImageUrl()).isEqualTo(adCreateDTO.getImageUrl());
        softly.assertThat(adEs.getAdUrl()).isEqualTo(adCreateDTO.getAdUrl());
        softly.assertThat(adEs.getDateRange().getFrom()).isEqualTo(adCreateDTO.getFrom());
        softly.assertThat(adEs.getDateRange().getTo()).isEqualTo(adCreateDTO.getTo());
        softly.assertThat(adEs.getDailyLimit()).isEqualTo(adCreateDTO.getDailyLimit());
        softly.assertThat(adEs.getSexesToMatch()).isEqualTo(adCreateDTO.getSexesToMatch());
        softly.assertThat(adEs.getAgeRangeToMatch().getFrom())
                .isEqualTo(20);
        softly.assertThat(adEs.getAgeRangeToMatch().getTo())
                .isEqualTo(Integer.MAX_VALUE);
        softly.assertThat(adEs.getTagsToMatch()).isEqualTo(adCreateDTO.getTagsToMatch());
        softly.assertAll();
    }

    @Test
    public void testCreateAdMatchingAgeHigher() {
        final AdCreateDTO adCreateDTO = new AdCreateDTO(
                AD_ID,
                "imageUrl",
                "adUrl",
                faker.date().future(5, TimeUnit.DAYS),
                faker.date().future(6, 5, TimeUnit.DAYS),
                5,
                null,
                null,
                30, null
        );

        final URI adCreateURI = this.thisBuildAdCreateURI();
        final ResponseEntity<Void> response = this.sendPostRequest(adCreateURI, adCreateDTO,
                createAuthToken(this.AGENT_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Optional<AdES> optionalAdES = adRepository.findById(AD_ID);
        assertTrue(optionalAdES.isPresent());

        AdES adEs = optionalAdES.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(adEs.getId()).isEqualTo(AD_ID);
        softly.assertThat(adEs.getImageUrl()).isEqualTo(adCreateDTO.getImageUrl());
        softly.assertThat(adEs.getAdUrl()).isEqualTo(adCreateDTO.getAdUrl());
        softly.assertThat(adEs.getDateRange().getFrom()).isEqualTo(adCreateDTO.getFrom());
        softly.assertThat(adEs.getDateRange().getTo()).isEqualTo(adCreateDTO.getTo());
        softly.assertThat(adEs.getDailyLimit()).isEqualTo(adCreateDTO.getDailyLimit());
        softly.assertThat(adEs.getSexesToMatch()).isEqualTo(adCreateDTO.getSexesToMatch());
        softly.assertThat(adEs.getAgeRangeToMatch().getFrom())
                .isEqualTo(0);
        softly.assertThat(adEs.getAgeRangeToMatch().getTo())
                .isEqualTo(30);
        softly.assertThat(adEs.getTagsToMatch()).isEqualTo(adCreateDTO.getTagsToMatch());
        softly.assertAll();
    }

    @Test
    public void testCreateAdMatchingTags() {
        final AdCreateDTO adCreateDTO = new AdCreateDTO(
                AD_ID,
                "imageUrl",
                "adUrl",
                faker.date().future(5, TimeUnit.DAYS),
                faker.date().future(6, 5, TimeUnit.DAYS),
                5,
                null,
                null,
                null, Set.of("t1", "t2", "t3")
        );

        final URI adCreateURI = this.thisBuildAdCreateURI();
        final ResponseEntity<Void> response = this.sendPostRequest(adCreateURI, adCreateDTO,
                createAuthToken(this.AGENT_ID, this.JWT_SECRET));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Optional<AdES> optionalAdES = adRepository.findById(AD_ID);
        assertTrue(optionalAdES.isPresent());

        AdES adEs = optionalAdES.get();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(adEs.getId()).isEqualTo(AD_ID);
        softly.assertThat(adEs.getImageUrl()).isEqualTo(adCreateDTO.getImageUrl());
        softly.assertThat(adEs.getAdUrl()).isEqualTo(adCreateDTO.getAdUrl());
        softly.assertThat(adEs.getDateRange().getFrom()).isEqualTo(adCreateDTO.getFrom());
        softly.assertThat(adEs.getDateRange().getTo()).isEqualTo(adCreateDTO.getTo());
        softly.assertThat(adEs.getDailyLimit()).isEqualTo(adCreateDTO.getDailyLimit());
        softly.assertThat(adEs.getSexesToMatch()).isEqualTo(adCreateDTO.getSexesToMatch());
        softly.assertThat(adEs.getAgeRangeToMatch().getFrom())
                .isEqualTo(Integer.MAX_VALUE);
        softly.assertThat(adEs.getAgeRangeToMatch().getTo())
                .isEqualTo(Integer.MAX_VALUE);
        softly.assertThat(adEs.getTagsToMatch()).isEqualTo(adCreateDTO.getTagsToMatch());
        softly.assertAll();
    }


    @Test
    public void testCreateAdNoAuth() {
        final AdCreateDTO adCreateDTO = new AdCreateDTO(
                AD_ID,
                "imageUrl",
                "adUrl",
                faker.date().future(5, TimeUnit.DAYS),
                faker.date().future(6, 5, TimeUnit.DAYS),
                5,
                Set.of("MALE", "FEMALE"),
                null,
                null, null
        );

        final URI adCreateURI = this.thisBuildAdCreateURI();
        final ResponseEntity<Void> response = this.sendPostRequest(adCreateURI, adCreateDTO);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

    }

    private URI thisBuildAdCreateURI() {
        final UriComponentsBuilder builder =
                UriComponentsBuilder.fromHttpUrl(
                        String.format("http://localhost:%d/api/v1/ads", this.port));
        return builder.build().encode().toUri();
    }

    private ResponseEntity<Void> sendPostRequest(
            final URI apiEndpoint, final AdCreateDTO requestBody) {
        final HttpEntity<AdCreateDTO> entity = new HttpEntity<>(requestBody);

        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.POST, entity, Void.class);
    }

    private ResponseEntity<Void> sendPostRequest(
            final URI apiEndpoint, final AdCreateDTO requestBody, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));

        final HttpEntity<AdCreateDTO> entity = new HttpEntity<>(requestBody, headers);

        return this.testRestTemplate.exchange(
                apiEndpoint, HttpMethod.POST, entity, Void.class);
    }

}
