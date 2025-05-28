package school.sorokin.event.manager.telegrambot.telegram.message;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import school.sorokin.event.manager.telegrambot.openai.ChatGptService;

@Service
@AllArgsConstructor
public class TelegramTextHandler {

    private final static String WRITE_YOUR_QUESTION = "напишите Ваш вопрос после @bot_username и я отвечу";
    private static final String TEMPLATE = "Hey %s\n\n%s";
    private static final String EMPTY_TEMPLATE = "%s, %s";
    private static final int MAX_WIDTH = 4096;

    private final ChatGptService gptService;

    public SendMessage processTextMessage(String query, Long chatId, String author) {
        if (query.isEmpty()) {
            return new SendMessage(
                    chatId.toString(),
                    getAnswer(EMPTY_TEMPLATE, author, WRITE_YOUR_QUESTION)
            );
        } else {
            return new SendMessage(
                    chatId.toString(),
                    getAnswer(TEMPLATE, author, gptService.getResponseChatForUser(chatId, query))
            );
        }
    }

    private String getAnswer(String template, String author, String content) {
        return StringUtils.abbreviate(
                String.format(
                        template,
                        author,
                        content
                ),
                MAX_WIDTH
        );
    }
}
