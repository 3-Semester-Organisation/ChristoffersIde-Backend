package ccy.reactiveprogramingmonoandflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

/*
 This DTO was made by ChatGPT 4.0 using this prompt
 https://chat.openai.com/share/457d16e0-5823-47ab-974b-373a423f9068
 */
@Getter
@Builder
@AllArgsConstructor
public class ChatCompletionRequest {

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


    private List<Message> messages;

    public ChatCompletionRequest() {
        this.messages = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
