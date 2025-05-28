package school.sorokin.event.manager.telegrambot.openai.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import school.sorokin.event.manager.telegrambot.common.AsyncOperationService;

import java.util.concurrent.CompletableFuture;

import static school.sorokin.event.manager.telegrambot.Const.OPENAI_COMPLETION_URL;

@Service
@Slf4j
public class OpenAIClient {

    private final String token;
    private final RestTemplate restTemplate;
    private final AsyncOperationService asyncOperationService;

    public OpenAIClient(
            @Value("${openai.token}") String token,
            AsyncOperationService asyncOperationService
    ) {
        this.token = token;
        this.restTemplate = new RestTemplate();
        this.asyncOperationService = asyncOperationService;
    }

    private ChatCompletionResponse createChatCompletion(
            ChatCompletionRequest request
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChatCompletionRequest> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ChatCompletionResponse> responseEntity = restTemplate.exchange(
                OPENAI_COMPLETION_URL,
                HttpMethod.POST,
                httpEntity,
                ChatCompletionResponse.class
        );
        return responseEntity.getBody();
    }

    public CompletableFuture<ChatCompletionResponse> createChatCompletionAsync(
            ChatCompletionRequest request
    ) {
        return asyncOperationService.executeAsync(
                () -> createChatCompletion(request),
                "OpenAI-ChatCompletion"
        );
    }
}
