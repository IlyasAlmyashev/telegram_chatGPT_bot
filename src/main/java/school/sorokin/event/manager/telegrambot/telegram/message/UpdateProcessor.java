package school.sorokin.event.manager.telegrambot.telegram.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import school.sorokin.event.manager.telegrambot.telegram.TelegramAsyncMessageSender;

import static school.sorokin.event.manager.telegrambot.Const.AT_SIGN;
import static school.sorokin.event.manager.telegrambot.Const.ERROR_MESSAGE;
import static school.sorokin.event.manager.telegrambot.Utils.getAuthor;

@Slf4j
@Service
public class UpdateProcessor {
    private static final String CHAT_PREFIX = "Hey chat, ";
    public static final String CONTENT_PREFIX = " Content: ";

    private final TelegramAsyncMessageSender telegramAsyncMessageSender;
    private final TelegramTextHandler telegramTextHandler;
    private final String botUsername;

    public UpdateProcessor(
            TelegramAsyncMessageSender telegramAsyncMessageSender,
            TelegramTextHandler telegramTextHandler,
            @Value("${bot.username}") String botUsername
    ) {
        this.telegramAsyncMessageSender = telegramAsyncMessageSender;
        this.telegramTextHandler = telegramTextHandler;
        this.botUsername = AT_SIGN + botUsername;
        log.info("Bot username initialized: {}", this.botUsername);
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        var message = update.getMessage();
        log.info("Message is received. message={}", message);
        var chatId = message.getChatId().toString();

        if (message.hasText() && message.getText().contains(botUsername)) {
            telegramAsyncMessageSender.sendMessageAsync(
                    chatId,
                    () -> handleMessageAsync(message),
                    (throwable) -> getErrorMessage(throwable, chatId)
            );
        }
        return null;
    }

    private SendMessage handleMessageAsync(Message message) {
        String query = buildQueryFromMessage(message);
        log.info("Query prepared: {}", query);

        SendMessage result = telegramTextHandler.processTextMessage(query, message.getChatId(), getAuthor(message));
        result.setParseMode(ParseMode.MARKDOWNV2);
        return result;
    }

    private String buildQueryFromMessage(Message message) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append(
                message.getText()
                        .replaceFirst(botUsername, CHAT_PREFIX)
                        .trim()
        );

        if (message.isReply()) {
            if (message.getReplyToMessage().hasText()) {
                queryBuilder.append(CONTENT_PREFIX);
                queryBuilder.append(
                        message.getReplyToMessage()
                                .getText()
                                .trim()
                );
            }
            if (message.getReplyToMessage().getCaption() != null) {
                queryBuilder.append(CONTENT_PREFIX);
                queryBuilder.append(
                        message.getReplyToMessage()
                                .getCaption()
                                .trim()
                );
            }
        }

        return queryBuilder.toString();
    }

    private SendMessage getErrorMessage(Throwable throwable, String chatId) {
        log.error("Произошла ошибка, chatId={}", chatId, throwable);
        return SendMessage.builder()
                .chatId(chatId)
                .text(ERROR_MESSAGE)
                .build();
    }

}
