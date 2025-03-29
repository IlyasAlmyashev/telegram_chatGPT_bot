package school.sorokin.event.manager.telegrambot.telegram.message;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import school.sorokin.event.manager.telegrambot.telegram.TelegramAsyncMessageSender;

import static school.sorokin.event.manager.telegrambot.Const.ERROR_MESSAGE;
import static school.sorokin.event.manager.telegrambot.Const.HEYBOT;

@Slf4j
@Service
@AllArgsConstructor
public class UpdateProcessor {

//    private final TelegramCommandsDispatcher telegramCommandsDispatcher;
    private final TelegramAsyncMessageSender telegramAsyncMessageSender;
    private final TelegramTextHandler telegramTextHandler;

    public BotApiMethod<?> handleUpdate(Update update) {
        var message = update.getMessage();
        log.trace("Message is received. message={}", message);
        var chatId = message.getChatId().toString();

//        if (telegramCommandsDispatcher.isCommand(message)) {
//            return telegramCommandsDispatcher.processCommand(message);
//        }

        if (message.hasText() && message.getText().startsWith(HEYBOT)) {
            telegramAsyncMessageSender.sendMessageAsync(
                    chatId,
                    () -> handleMessageAsync(message),
                    (throwable) -> getErrorMessage(throwable, chatId)
            );
        }
        return null;
    }

    private SendMessage handleMessageAsync(Message message) {
        SendMessage result = telegramTextHandler.processTextMessage(message);

        result.setParseMode(ParseMode.MARKDOWNV2);
        return result;
    }

    private SendMessage getErrorMessage(Throwable throwable, String chatId) {
        log.error("Произошла ошибка, chatId={}", chatId, throwable);
        return SendMessage.builder()
                .chatId(chatId)
                .text(ERROR_MESSAGE)
                .build();
    }

}
