package ccy.reactiveprogramingmonoandflux.api;

import ccy.reactiveprogramingmonoandflux.dto.AgeifyResponse;
import ccy.reactiveprogramingmonoandflux.dto.GenderizeResponse;
import ccy.reactiveprogramingmonoandflux.dto.NameInfoResponse;
import ccy.reactiveprogramingmonoandflux.dto.NationalizeResponse;
import ccy.reactiveprogramingmonoandflux.service.ProbableDemographicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class NameInfoController {


    private final ProbableDemographicProfileService service;


    @GetMapping("/name-info")
    public ResponseEntity<NameInfoResponse> getNameInfo(@RequestParam String name) {

        boolean doesExist = service.doesExist(name);
        NameInfoResponse response;
        System.out.println(doesExist);
        if (doesExist) {
            System.out.println("made use of caching");
            response = service.getNameInfoResponse(name);

        } else {
            System.out.println("No instance of " + name + " in cache");
            response = getNameInfoResponseNonBlocking(name);
        }

        return ResponseEntity.ok(response);
    }


    private NameInfoResponse getNameInfoResponseNonBlocking(String name) {
        Mono<AgeifyResponse> potentialAge = WebClient.create()
                .get()
                .uri("https://api.agify.io?name=" + name)
                .retrieve()
                .bodyToMono(AgeifyResponse.class)
                .doOnError(error -> System.out.println("Ageify Error:" + error));

        Mono<GenderizeResponse> potentialGender = WebClient.create()
                .get()
                .uri("https://api.genderize.io?name=" + name)
                .retrieve()
                .bodyToMono(GenderizeResponse.class)
                .doOnError(error -> System.out.println("Genderize Error:" + error));

        Mono<NationalizeResponse> potentialNationality = WebClient.create()
                .get()
                .uri("https://api.nationalize.io/?name=" + name)
                .retrieve()
                .bodyToMono(NationalizeResponse.class)
                .doOnError(error -> System.out.println("Nationalize Error:" + error));

        long startTime = System.currentTimeMillis();
        Mono<NameInfoResponse> nameInfoMono = Mono.zip(potentialAge, potentialGender, potentialNationality)
                .map(tuple3 -> new NameInfoResponse(
                        name,
                        tuple3.getT2().gender(),
                        tuple3.getT2().probability(),
                        tuple3.getT1().age(),
                        tuple3.getT3().country()
                ));

        NameInfoResponse nameInfoResponse = nameInfoMono.block();

        long finishedTime = System.currentTimeMillis();
        long processingTime = finishedTime - startTime;
        System.out.println(processingTime + "ms");

        NameInfoResponse response = service.create(nameInfoResponse);
        return response;
    }
}