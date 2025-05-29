package school.sorokin.event.manager.telegrambot.telegram;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import school.sorokin.event.manager.telegrambot.common.AsyncOperationService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Function;
import java.util.function.Supplier;

import static school.sorokin.event.manager.telegrambot.Const.WAIT_MESSAGE;

@Slf4j
@Service
public class TelegramAsyncMessageSender {

    private final DefaultAbsSender defaultAbsSender;
    private final AsyncOperationService asyncOperationService;

    public TelegramAsyncMessageSender(
            @Lazy @Qualifier("defaultAbsSender") DefaultAbsSender defaultAbsSender,
            AsyncOperationService asyncOperationService) {
        this.defaultAbsSender = defaultAbsSender;
        this.asyncOperationService = asyncOperationService;
    }

    /**
     * Отправляет асинхронное сообщение в Telegram.
     *
     * @param chatId         ID чата, куда будет отправлено сообщение.
     * @param action         Функция, которая возвращает объект SendMessage для
     *                       отправки.
     * @param onErrorHandler Функция, которая обрабатывает ошибки и возвращает
     *                       SendMessage с сообщением об ошибке.
     */
    @SneakyThrows
    public void sendMessageAsync(
            String chatId,
            Supplier<SendMessage> action,
            Function<Throwable, SendMessage> onErrorHandler) {
        log.info("Send message async: chatId={}", chatId);
        var message = defaultAbsSender.execute(
                SendMessage.builder()
                        .text(WAIT_MESSAGE)
                        .chatId(chatId)
                        .build());

        asyncOperationService
                .executeAsync(action, "Telegram-Message")
                .exceptionally(onErrorHandler)
                .thenAccept(sendMessage -> updateMessage(chatId, message.getMessageId(), sendMessage));
    }

    private void updateMessage(String chatId, Integer messageId, SendMessage sendMessage) {
        try {
            log.info("Send edit message async: chatId={}", chatId);
            defaultAbsSender.execute(EditMessageText.builder()
                    .chatId(chatId)
                    .messageId(messageId)
                    .text(sendMessage.getText())
                    .build());
        } catch (TelegramApiException e) {
            log.error("Error while send request to telegram", e);
            throw new RuntimeException(e);
        }
    }
}
