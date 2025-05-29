package school.sorokin.event.manager.telegrambot.openai.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
            AsyncOperationService asyncOperationService) {
        this.token = token;
        this.restTemplate = new RestTemplate();
        this.asyncOperationService = asyncOperationService;
    }

    public CompletableFuture<ChatCompletionResponse> createChatCompletionAsync(
            ChatCompletionRequest request) {
        return asyncOperationService.executeAsync(
                () -> createChatCompletion(request),
                "OpenAI-ChatCompletion");
    }

    private ChatCompletionResponse createChatCompletion(
            ChatCompletionRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        if (request.contents() != null && !request.contents().isEmpty()) {
            // Если есть файлы, используем multipart/form-data
            return getMultipartResponse(request, headers);
        } else {
            // Для обычных текстовых запросов используем JSON
            return getJsonResponse(request, headers);
        }
    }

    private ChatCompletionResponse getJsonResponse(
            ChatCompletionRequest request,
            HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ChatCompletionRequest> httpEntity = new HttpEntity<>(request, headers);
        ResponseEntity<ChatCompletionResponse> responseEntity = restTemplate.exchange(
                OPENAI_COMPLETION_URL,
                HttpMethod.POST,
                httpEntity,
                ChatCompletionResponse.class);
        return responseEntity.getBody();
    }

    private ChatCompletionResponse getMultipartResponse(
            ChatCompletionRequest request,
            HttpHeaders headers) {
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Добавляем основные данные запроса
        body.add("model", request.model());
        body.add("messages", request.messages());

        // Добавляем файлы
        for (int i = 0; i < request.contents().size(); i++) {
            MessageContent content = request.contents().get(i);
            if (content.bytes() != null) {
                int finalI = i;
                ByteArrayResource resource = new ByteArrayResource(content.bytes()) {
                    @Override
                    public String getFilename() {
                        return "file" + finalI;
                    }
                };
                body.add("files", resource);
            }
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<ChatCompletionResponse> responseEntity = restTemplate.exchange(
                OPENAI_COMPLETION_URL,
                HttpMethod.POST,
                requestEntity,
                ChatCompletionResponse.class);
        return responseEntity.getBody();
    }
}