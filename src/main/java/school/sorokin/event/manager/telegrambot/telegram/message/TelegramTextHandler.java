package school.sorokin.event.manager.telegrambot.telegram.message;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import school.sorokin.event.manager.telegrambot.openai.ChatGptService;

import static school.sorokin.event.manager.telegrambot.Const.HEYBOT;
import static school.sorokin.event.manager.telegrambot.Utils.getAuthor;

@Service
@AllArgsConstructor
public class TelegramTextHandler {

    private final static String WRITE_YOUR_QUESTION = "напишите Ваш вопрос после команды и я отвечу";
    private static final String TEMPLATE = "%s, вот ответ на Ваш вопрос: \n\n%s";
    private static final String EMPTY_TEMPLATE = "%s, %s";
    private static final int MAX_WIDTH = 4096;

    private final ChatGptService gptService;

    public SendMessage processTextMessage(Message message) {
        var messageText = message.getText();
        var chatId = message.getChatId();

        if (messageText.startsWith(HEYBOT)) {
            String query = messageText.replaceFirst(HEYBOT, "").trim();

            if (query.isEmpty()) {
                return new SendMessage(
                        chatId.toString(),
                        getAnswer(EMPTY_TEMPLATE, message, WRITE_YOUR_QUESTION)
                );
            } else {
                return new SendMessage(
                        chatId.toString(),
                        getAnswer(TEMPLATE, message, gptService.getResponseChatForUser(chatId, query))
                );
            }
        }

        return null;
    }

    private String getAnswer(String template, Message message, String content) {
        return StringUtils.abbreviate(
                String.format(
                        template,
                        getAuthor(message),
                        content
                ),
                MAX_WIDTH
        );
    }

}
