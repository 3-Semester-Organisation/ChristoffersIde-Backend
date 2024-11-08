package ccy.reactiveprogramingmonoandflux.api;

import ccy.reactiveprogramingmonoandflux.dto.*;
import ccy.reactiveprogramingmonoandflux.exception.ApiConnectivityException;
import ccy.reactiveprogramingmonoandflux.exception.NameDataNotFoundException;
import ccy.reactiveprogramingmonoandflux.service.nameinfo.ProbableDemographicProfileService;
import ccy.reactiveprogramingmonoandflux.service.openai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class RecipeByIndividualNameController {


    private final ProbableDemographicProfileService service;
    private final OpenAiService openAiService;

    ///recipe-by-individual?name=NAME&country=COUNTRY_ID
    @GetMapping("/recipe-by-individual")
    public ResponseEntity<MyResponse> getNameInfo(@RequestParam String name) {

        boolean doesExist = service.doesExist(name);
        NameInfoResponse nameInfoResponse;
        System.out.println(doesExist);
        if (doesExist) {
            System.out.println("made use of caching");
            nameInfoResponse = service.getNameInfoResponse(name);
            MyResponse response = openAiService.makeRequest(nameInfoResponse);
            return ResponseEntity.ok(response);
        } else {
            System.out.println("No instance of " + name + " in cache");

            nameInfoResponse = getNameInfoResponseNonBlocking(name);

            MyResponse response = openAiService.makeRequest(nameInfoResponse);
            return ResponseEntity.ok(response);
        }
    }



    @PostMapping("/recipe-by-specification")
    public ResponseEntity<MyResponse> getUserSpecificRecipe(@RequestBody UserSpecifications specifications) {
        MyResponse response = openAiService.makeRequest(specifications);
        return ResponseEntity.ok(response);
    }



    private NameInfoResponse getNameInfoResponseNonBlocking(String name) {
        //Find nationalitet først. Nationalitet bruges i de andre api'er
        Mono<NationalizeResponse> potentialNationality = WebClient.create()
                .get()
                .uri("https://api.nationalize.io/?name=" + name)
                .retrieve()
                .bodyToMono(NationalizeResponse.class)
                .onErrorMap(error -> {
                    System.out.println("Nationalize Error: " + error.getMessage());
                    throw new ApiConnectivityException("Failed to fetch nationality data (might have run out of tokens)");
                });

        NationalizeResponse countryResp = potentialNationality
                .map(tuple -> new NationalizeResponse(tuple.country()))
                .block();

        List<CountryDto> countryDtoList = countryResp.country();
        System.out.println(countryDtoList);

        if (countryDtoList.isEmpty()) {
            throw new NameDataNotFoundException("Mamma mia! I couldn’t-a whip up that recipe! No country found by this name");
        }
        String countryId = countryDtoList.getFirst().country_id();


        Mono<AgeifyResponse> potentialAge = WebClient.create()
                .get()
                .uri("https://api.agify.io?name=" + name + "&country_id=" + countryId)
                .retrieve()
                .bodyToMono(AgeifyResponse.class)
                .onErrorMap(error -> {
                    System.out.println("Ageify Error: " + error.getMessage());
                    throw new ApiConnectivityException("Failed to fetch age data (might have run out of tokens)");
                });

        Mono<GenderizeResponse> potentialGender = WebClient.create()
                .get()
                .uri("https://api.genderize.io?name=" + name + "&country_id=" + countryId)
                .retrieve()
                .bodyToMono(GenderizeResponse.class)
                .onErrorMap(error -> {
                    System.out.println("Genderize Error: " + error.getMessage());
                    throw new ApiConnectivityException("Failed to fetch gender data (might have run out of tokens)");
                });

        long startTime = System.currentTimeMillis();

        Mono<NameInfoResponse> nameInfoMono = Mono.zip(potentialAge, potentialGender)
                .map(tuple -> new NameInfoResponse(
                        name,
                        tuple.getT2().gender(),
                        tuple.getT2().probability(),
                        tuple.getT1().age(),
                        countryDtoList
                ));

        NameInfoResponse nameInfoResponse = nameInfoMono.block();

        if (nameInfoResponse.gender() == null) {
            throw new NameDataNotFoundException("Mamma mia! I couldn’t-a whip up that recipe! No gender found by this name");
        }
        if (nameInfoResponse.age() == 0) {
            throw new NameDataNotFoundException("Mamma mia! I couldn’t-a whip up that recipe! No age found by this name");
        }

        long finishedTime = System.currentTimeMillis();
        long processingTime = finishedTime - startTime;
        System.out.println(processingTime + "ms");

        NameInfoResponse response = service.create(nameInfoResponse);
        return response;
    }
}