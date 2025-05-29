package school.sorokin.event.manager.telegrambot.telegram.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import school.sorokin.event.manager.telegrambot.openai.api.MessageContent;
import school.sorokin.event.manager.telegrambot.telegram.TelegramAsyncMessageSender;
import school.sorokin.event.manager.telegrambot.telegram.TelegramBot;
import school.sorokin.event.manager.telegrambot.telegram.service.TelegramFileService;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static school.sorokin.event.manager.telegrambot.Const.AT_SIGN;
import static school.sorokin.event.manager.telegrambot.Const.ERROR_MESSAGE;
import static school.sorokin.event.manager.telegrambot.Utils.getAuthor;

@Slf4j
@Service
public class UpdateProcessor {
    private static final String CHAT_PREFIX = "Hey chat, ";
    private static final String CONTENT_PREFIX = " Content: ";

    private final TelegramFileService telegramFileService;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;
    private final TelegramTextHandler telegramTextHandler;
    private final String botUsername;

    public UpdateProcessor(
            TelegramFileService telegramFileService,
            TelegramAsyncMessageSender telegramAsyncMessageSender,
            TelegramTextHandler telegramTextHandler,
            @Value("${bot.username}") String botUsername) {
        this.telegramFileService = telegramFileService;
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
                    (throwable) -> getErrorMessage(throwable, chatId));
        }
        return null;
    }

    private SendMessage handleMessageAsync(Message message) {
        // Формируем текстовый запрос
        String query = buildQueryFromMessage(message);
        log.info("Query prepared: {}", query);

        // Извлекаем файлы/изображения (если есть)
        var contents = extractContents(message);

        // Передаем текст и контент в TelegramTextHandler
        // Предполагаем, что вы доработаете processTextMessage
        // чтобы он принимал contents и далее в ChatGptService
        SendMessage result = telegramTextHandler.processTextMessage(
                query,
                message.getChatId(),
                getAuthor(message),
                contents);
        result.setParseMode(ParseMode.MARKDOWNV2);

        return result;
    }

    /**
     * Сбор информации о файлах/изображениях
     */
    private List<MessageContent> extractContents(Message message) {
        List<MessageContent> messageContents = new ArrayList<>();

        if (message.isReply()) {
            messageContents.addAll(
                    extractContents(
                            message.getReplyToMessage()));
        }

        // Документ
        if (message.hasDocument()) {
            var document = message.getDocument();
            // Загрузите байты любым доступным способом (Placeholder):
            byte[] docBytes = telegramFileService.downloadFileBytes(document.getFileId());

            messageContents.add(
                    MessageContent.builder()
                            .type("document")
                            .fileName(document.getFileName())
                            .bytes(docBytes)
                            .mimeType(document.getMimeType())
                            .build());
        }

        // Фото
        if (message.hasPhoto()) {
            var photos = message.getPhoto();
            if (!photos.isEmpty()) {
                // Обычно самое большое фото – последнее в списке
                var largestPhoto = photos.get(photos.size() - 1);
                // Загрузите байты (Placeholder):
                byte[] photoBytes = telegramFileService.downloadFileBytes(largestPhoto.getFileId());

                messageContents.add(
                        MessageContent.builder()
                                .type("photo")
                                .fileName("photo_" + largestPhoto.getFileId())
                                .bytes(photoBytes)
                                .mimeType("image/jpeg")
                                .build());
            }
        }

        return messageContents;
    }

    private String buildQueryFromMessage(Message message) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder
                .append(CHAT_PREFIX)
                .append(
                        message.getText()
                                .replaceFirst(botUsername, "")
                                .trim());

        if (message.isReply()) {
            Message replyMessage = message.getReplyToMessage();

            if (replyMessage.hasText()) {
                queryBuilder
                        .append(CONTENT_PREFIX)
                        .append(
                                message.getReplyToMessage()
                                        .getText()
                                        .trim());
            }
            if (replyMessage.getCaption() != null) {
                queryBuilder
                        .append(CONTENT_PREFIX)
                        .append(
                                message.getReplyToMessage()
                                        .getCaption()
                                        .trim());
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