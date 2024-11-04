package ccy.reactiveprogramingmonoandflux;

import ccy.reactiveprogramingmonoandflux.dto.GenderizeResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RemoteApiTester implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        //...
//        System.out.println(callSlowEndpoint().toString());
//
//        String randomString1 = callSlowEndpoint().block();
//        String randomString2 = callSlowEndpoint().block();
//        String randomString3 = callSlowEndpoint().block();
//        System.out.println(randomString1 + " " + randomString2 + " " + randomString3 + "\n");

//        callEndpointBlocking();
//        callSlowEndpointNonBlocking();

//        GenderizeResponse response = getGenderForName("Christoffer").block();
//        System.out.println(response);


    }

    private Mono<String> callSlowEndpoint(){
        Mono<String> slowResponse = WebClient.create()
                .get()
                .uri("http://localhost:8080/random-string-slow")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e-> System.out.println("UUUPS : "+e.getMessage()));

        return slowResponse;
    }

    public void callEndpointBlocking() {
        long start = System.currentTimeMillis();
        List<String> randomStrings = new ArrayList<>();

        Mono<String> slowResponse = callSlowEndpoint();
        randomStrings.add(slowResponse.block()); //Three seconds spent

        slowResponse = callSlowEndpoint();
        randomStrings.add(slowResponse.block());//Three seconds spent

        slowResponse = callSlowEndpoint();
        randomStrings.add(slowResponse.block());//Three seconds spent
        long end = System.currentTimeMillis();
        randomStrings.add(0, "Time spent BLOCKING: " + (end - start) + "ms");

        //System.out.println(randomStrings.stream().collect(Collectors.joining(",")));
        System.out.println(String.join(", ", randomStrings));
    }


    public void callSlowEndpointNonBlocking(){
        List<String> randomStrings = new ArrayList<>();

        long start = System.currentTimeMillis();

        Mono<String> sr1 = callSlowEndpoint();
        Mono<String> sr2 = callSlowEndpoint();
        Mono<String> sr3 = callSlowEndpoint();

        var rs = Mono.zip(sr1,sr2,sr3).map(tuple3 -> {

            randomStrings.add(tuple3.getT1());
            randomStrings.add(tuple3.getT2());
            randomStrings.add(tuple3.getT3());

            return randomStrings;
        });
        List<String> randoms = rs.block(); //We only block when all the three Mono's has fulfilled

        long end = System.currentTimeMillis();
        randomStrings.add(0,"Time spent NON-BLOCKING: "+(end-start) + "ms");

        System.out.println(randoms.stream().collect(Collectors.joining(", ")));
    }


    Mono<GenderizeResponse> getGenderForName(String name) {
        WebClient client = WebClient.create();
        Mono<GenderizeResponse> gender = client.get()
                .uri("https://api.genderize.io?name="+name)
                .retrieve()
                .bodyToMono(GenderizeResponse.class);
        return gender;
    }





    List<String> names = new ArrayList<>(List.of("lars", "peter", "sanne", "kim", "david", "maja"));

    public void getGendersBlocking() {
        long start = System.currentTimeMillis();
        List<GenderizeResponse> genders = names.stream().map(name -> getGenderForName(name).block()).toList();
        long end = System.currentTimeMillis();
        System.out.println("Time for six external requests, BLOCKING: "+ (end-start));
    }

    public void getGendersNonBlocking() {
        long start = System.currentTimeMillis();
        var genders = names.stream().map(name -> getGenderForName(name)).toList();
        Flux<GenderizeResponse> flux = Flux.merge(Flux.concat(genders));
        List<GenderizeResponse> response = flux.collectList().block();
        long end = System.currentTimeMillis();
        System.out.println(response);
        System.out.println("Time for six external requests, NON-BLOCKING: "+ (end-start));
    }
}