package school.sorokin.event.manager.telegrambot.openai;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import school.sorokin.event.manager.telegrambot.openai.api.ChatCompletionRequest;
import school.sorokin.event.manager.telegrambot.openai.api.Message;
import school.sorokin.event.manager.telegrambot.openai.api.MessageContent;
import school.sorokin.event.manager.telegrambot.openai.api.OpenAIClient;

import java.util.List;

import static school.sorokin.event.manager.telegrambot.Const.MODEL_4O_MINI;

@Service
@AllArgsConstructor
public class ChatGptService {

    private final OpenAIClient openAIClient;
    private final ChatGptHistoryService chatGptHistoryService;

    @Nonnull
    @SneakyThrows
    public String getResponseChatForUser(
            Long userId,
            String userTextInput,
            List<MessageContent> contents) {
        chatGptHistoryService.createHistoryIfNotExist(userId);
        var history = chatGptHistoryService.addMessageToHistory(
                userId,
                Message.builder()
                        .content(userTextInput)
                        .role("user")
                        .build());

        var request = ChatCompletionRequest.builder()
                .model(MODEL_4O_MINI)
                .messages(history.chatMessages())
                .contents(contents) // <-- сюда передаем файлы/изображения
                .build();

        var response = openAIClient.createChatCompletionAsync(request);

        var messageFromGpt = response.get()
                .choices()
                .get(0)
                .message();

        chatGptHistoryService.addMessageToHistory(userId, messageFromGpt);

        return messageFromGpt.content();
    }
}
