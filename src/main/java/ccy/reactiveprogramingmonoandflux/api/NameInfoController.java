package ccy.reactiveprogramingmonoandflux.api;

import ccy.reactiveprogramingmonoandflux.dto.*;
import ccy.reactiveprogramingmonoandflux.service.nameinfo.ProbableDemographicProfileService;
import ccy.reactiveprogramingmonoandflux.service.openai.OpenAiService;
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
    private final OpenAiService openAiService;


    final static String SYSTEM_MESSAGE = """
                          You are a helpful assistant that provides the most likely recipe for a diner that an individual would like, given the age, country and gender, for an individual
                          You should be friendly and helpful, and provide useful information to the user.
                          You should provide information that is relevant to the user's questions and help them with their dinner plans.
                          If the user asks questions not related to food or dinner plans, you should politely guide them back to the main topic.
                          """;


    @GetMapping("/recipe-by-individual")
    public ResponseEntity<MyResponse> getNameInfo(@RequestParam String name) {

        boolean doesExist = service.doesExist(name);
        NameInfoResponse nameInfoResponse;
        System.out.println(doesExist);
        if (doesExist) {
            System.out.println("made use of caching");
            nameInfoResponse = service.getNameInfoResponse(name);

        } else {
            System.out.println("No instance of " + name + " in cache");
            nameInfoResponse = getNameInfoResponseNonBlocking(name);
        }

        MyResponse response = openAiService.makeRequest(nameInfoResponse, SYSTEM_MESSAGE);

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