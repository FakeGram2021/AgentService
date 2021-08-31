package tim6.agentservice.adapter.http.controller;

import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tim6.agentservice.adapter.elasticsearch.service.AdService;
import tim6.agentservice.adapter.http.dto.AdCreateDTO;
import tim6.agentservice.adapter.http.dto.AdGetDTO;
import tim6.agentservice.adapter.http.helpers.AuthHelper;
import tim6.agentservice.domain.model.Ad;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/v1/ads")
public class AdController {

    private final AdService adService;

    @Autowired
    public AdController(AdService adService) {
        this.adService = adService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> createAd(
            @Valid @RequestBody final AdCreateDTO adCreateDTO) {
        final String authedUserId = AuthHelper.getCurrentUserId();
        final Ad ad = AdCreateDTO.to(adCreateDTO, authedUserId);

        adService.createAd(ad);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<AdGetDTO>> getAds() {
        final String authedUserId = AuthHelper.getCurrentUserId();
        List<Ad> ads = adService.getAdsForUserId(authedUserId);
        List<AdGetDTO> adGetDTOs = ads.stream().map(AdGetDTO::from).collect(Collectors.toList());

        return new ResponseEntity<>(adGetDTOs, HttpStatus.OK);
    }

}
