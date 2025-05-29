package school.sorokin.event.manager.telegrambot.telegram.message;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import school.sorokin.event.manager.telegrambot.openai.ChatGptService;
import school.sorokin.event.manager.telegrambot.openai.api.MessageContent;

import java.util.List;

@Service
@AllArgsConstructor
public class TelegramTextHandler {

    private static final String WRITE_YOUR_QUESTION = "Write your question after @bot_username and I will respond to you.";
    private static final String TEMPLATE = "Hey %s\n\n%s";
    private static final String EMPTY_TEMPLATE = "%s, %s";
    private static final int MAX_WIDTH = 4096;

    private final ChatGptService gptService;

    public SendMessage processTextMessage(
            String query,
            Long chatId,
            String author,
            List<MessageContent> contents) {
        // Делаем вызов ChatGptService, передавая query + contents
        // Если query пустой, то возвращаем сообщение с просьбой написать вопрос

        if (query.isEmpty()) {
            return new SendMessage(
                    chatId.toString(),
                    getAnswer(EMPTY_TEMPLATE, author, WRITE_YOUR_QUESTION));
        } else {
            return new SendMessage(
                    chatId.toString(),
                    getAnswer(TEMPLATE, author, gptService.getResponseChatForUser(chatId, query, contents)));
        }
    }

    private String getAnswer(String template, String author, String content) {
        return StringUtils.abbreviate(
                String.format(
                        template,
                        author,
                        content),
                MAX_WIDTH);
    }
}
