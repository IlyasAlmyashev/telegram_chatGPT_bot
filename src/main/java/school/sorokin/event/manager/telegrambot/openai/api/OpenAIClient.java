package school.sorokin.event.manager.telegrambot.openai.api;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@AllArgsConstructor
public class OpenAIClient {

    public static final String URL = "https://api.openai.com/v1/chat/completions";
    private final String token;
    private final RestTemplate restTemplate;

    public ChatCompletionResponse createChatCompletion(
            ChatCompletionRequest request
    ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token);
        httpHeaders.set("Content-type", "application/json");

        HttpEntity<ChatCompletionRequest> httpEntity = new HttpEntity<>(request, httpHeaders);

        ResponseEntity<ChatCompletionResponse> responseEntity = restTemplate.exchange(
                URL,
                HttpMethod.POST,
                httpEntity,
                ChatCompletionResponse.class
        );
        return responseEntity.getBody();
    }

}
