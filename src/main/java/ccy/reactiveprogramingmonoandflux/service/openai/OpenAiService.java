package ccy.reactiveprogramingmonoandflux.service.openai;

import ccy.reactiveprogramingmonoandflux.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

/**
 * This code utilizes WebClient along with several other classes from org.springframework.web.reactive.
 * However, the code is NOT reactive due to the use of the block() method, which bridges the reactive code (WebClient)
 * to our imperative code (the way we have used Spring Boot up until now).
 * <p>
 * You will not truly benefit from WebClient unless you need to make several external requests in parallel.
 * Additionally, the WebClient API is very clean, so if you are familiar with HTTP, it should be easy to
 * understand what's going on in this code.
 */

@Service
public class OpenAiService {

    public static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);

    final static String SYSTEM_MESSAGE_FOR_RECIPE_BY_NAME = """
            You are a helpful assistant that provides the most likely recipe for a dinner that an individual would like, given the age, country code and gender, for an individual.
            Each of the three variable must be weighted equally.
            You should provide information that is relevant to the user's questions and help them with their dinner plans.
            You should return the answer in JSON with the following format:
            {
                "title": "Title of the dish",
                "description": "Short description of the dish",
                "ingredients": {
                    "ingredient1": "amount",
                    "ingredient2": "amount",
                    ...
                },
                "instructions": [
                    "Step 1",
                    "Step 2",
                    ...
                ]
            }
            """;
    // TODO: !!! OBS: HAR ÆNDRET SIDSTE LINJE I PROMPT FOR TESTING !!!

    final static String SYSTEM_MESSAGE_FOR_RECIPE_BY_SPECIFICATIONS = """
            You are a helpful assistant that provides a recipe given a list of ingredients and some dietary restrictions.
            All ingredients has to be included in the recipe, and dietary restrictions must be taken into account.
            You should provide information that is relevant to the user's questions and help them with their dinner plans.
            You should return the answer in JSON with the following format:
            {
                "title": "Title of the dish",
                "description": "Short description of the dish",
                "ingredients": {
                    "ingredient1": "amount",
                    "ingredient2": "amount",
                    ...
                },
                "instructions": [
                    "Step 1",
                    "Step 2",
                    ...
                ]
            }
            """;
    // TODO: !!! OBS: HAR ÆNDRET SIDSTE LINJE I PROMPT FOR TESTING !!!

    @Value("${app.model}")
    private String MODEL;

    @Value("${app.temperature}")
    private double TEMPERATURE;

    @Value("${app.max_tokens}")
    private int MAX_TOKENS;

    @Value("${app.frequency_penalty}")
    private double FREQUENCY_PENALTY;

    @Value("${app.presence_penalty}")
    private double PRESENCE_PENALTY;

    @Value("${app.top_p}")
    private double TOP_P;

    @Value("${app.api-key}")
    private String API_KEY;

    //See here for a decent explanation of the parameters send to the API via the requestBody
    //https://platform.openai.com/docs/api-reference/completions/create

    @Value("${app.url}")
    public String URL;


    private final WebClient client;

    public OpenAiService() {
        this.client = WebClient.create();
    }

    //Use this constructor for testing, to inject a mock client
    public OpenAiService(WebClient client) {
        this.client = client;
    }


    public MyResponse makeRequest(NameInfoResponse nameInfo) {

        String prompt = createPromptFrom(nameInfo);

        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(MODEL);
        request.setTemperature(TEMPERATURE);
        request.setMax_tokens(MAX_TOKENS);
        request.setTop_p(TOP_P);
        request.setFrequency_penalty(FREQUENCY_PENALTY);
        request.setPresence_penalty(PRESENCE_PENALTY);
        request.getMessages().add(new ChatCompletionRequest.Message("system", SYSTEM_MESSAGE_FOR_RECIPE_BY_NAME));
        request.getMessages().add(new ChatCompletionRequest.Message("user", prompt));

        System.out.println(request.getModel());
        ObjectMapper mapper = new ObjectMapper();
        String json;
        String err;
        try {
            json = mapper.writeValueAsString(request);
            ChatCompletionResponse response = client.post()
                    .uri(new URI(URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .block();

            String responseMsg = response.getChoices().get(0).getMessage().getContent();
            int tokensUsed = response.getUsage().getTotal_tokens();
            System.out.print("Tokens used: " + tokensUsed);
            System.out.print(". Cost ($0.0015 / 1K tokens) : $" + String.format("%6f", (tokensUsed * 0.0015 / 1000)));
            System.out.println(". For 1$, this is the amount of similar requests you can make: " + Math.round(1 / (tokensUsed * 0.0015 / 1000)));
            return new MyResponse(responseMsg);

        } catch (WebClientResponseException e) {
            //This is how you can get the status code and message reported back by the remote API
            logger.error("Error response status code: " + e.getRawStatusCode());
            logger.error("Error response body: " + e.getResponseBodyAsString());
            logger.error("WebClientResponseException", e);
            err = "Internal Server Error, due to a failed request to external service. You could try again" +
                    "( While you develop, make sure to consult the detailed error message on your backend)";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, err);

        } catch (Exception e) {
            logger.error("Exception", e);
            err = "Internal Server Error - You could try again" +
                    "( While you develop, make sure to consult the detailed error message on your backend)";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, err);
        }
    }


    public MyResponse makeRequest(UserSpecifications specifications) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(MODEL);
        request.setTemperature(TEMPERATURE);
        request.setMax_tokens(MAX_TOKENS);
        request.setTop_p(TOP_P);
        request.setFrequency_penalty(FREQUENCY_PENALTY);
        request.setPresence_penalty(PRESENCE_PENALTY);
        String prompt = createPromptFrom(specifications);

        request.getMessages().add(new ChatCompletionRequest.Message("system", SYSTEM_MESSAGE_FOR_RECIPE_BY_SPECIFICATIONS));
        request.getMessages().add(new ChatCompletionRequest.Message("user", prompt));

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(request);
            ChatCompletionResponse openAiApiResponse = client.post()
                    .uri(new URI(URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(ChatCompletionResponse.class)
                    .block();

            String responseMsg = openAiApiResponse.getChoices().get(0).getMessage().getContent();

            MyResponse response = new MyResponse(responseMsg);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Something went wrong");
        }
    }


    public String createPromptFrom(NameInfoResponse nameInfo) {
        return "Provide a recipe given the age of " + nameInfo.age() + ", the gender of " + nameInfo.gender() + ", and the country code of " + nameInfo.countryList().getFirst() + ".";
    }

    public String createPromptFrom(UserSpecifications specifications) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("Given this list of ingredients: ");
        for (String ingredient : specifications.ingredients()) {
            prompt.append(ingredient).append(", ");
        }

        DietaryRequirement dietaryRequirement = specifications.dietaryRequirement();
        boolean isVegan = dietaryRequirement.vegan();
        boolean isVegetarian = dietaryRequirement.vegetarian();
        boolean isLactoseIntolerant = dietaryRequirement.lactoseIntolerant();
        boolean isGlutenIntolerant = dietaryRequirement.glutenIntolerant();

        prompt.append("and these dietary limitations: ");
        prompt.append("isVegan: ").append(isVegan).append(", ");
        prompt.append("isVegetarian: ").append(isVegetarian).append(", ");
        prompt.append("isLactoseIntolerant: ").append(isLactoseIntolerant).append(", ");
        prompt.append("isGlutenIntolerant: ").append(isGlutenIntolerant).append(". ");

        prompt.append("Please create a recipe that makes use of the given list of ingredients and that considers the dietary limitations.");

        return prompt.toString();
    }
}