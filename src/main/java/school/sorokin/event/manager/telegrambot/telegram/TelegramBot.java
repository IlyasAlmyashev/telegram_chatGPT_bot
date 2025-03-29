package school.sorokin.event.manager.telegrambot.telegram;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import school.sorokin.event.manager.telegrambot.telegram.message.UpdateProcessor;

import static school.sorokin.event.manager.telegrambot.Const.ERROR_MESSAGE;
import static school.sorokin.event.manager.telegrambot.Const.UPDATE_ERROR_MESSAGE;
import static school.sorokin.event.manager.telegrambot.Const.WEBHOOK_INIT_ERROR_MESSAGE;

@Slf4j
@Component
public class TelegramBot extends TelegramWebhookBot {

    @Value("${bot.url}")
    private String botUrl;
    @Value("${bot.username}")
    private String botUsername;
    private final UpdateProcessor updateProcessor;

    public TelegramBot(
            @Value("${bot.token}") String botToken,
            UpdateProcessor updateProcessor
    ) {
        super(new DefaultBotOptions(), botToken);
        this.updateProcessor = updateProcessor;
    }

    @PostConstruct
    public void init() {
        try {
            this.setWebhook(SetWebhook.builder()
                    .url(botUrl)
                    .build());
        } catch (TelegramApiException e) {
            log.error(WEBHOOK_INIT_ERROR_MESSAGE, e);
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {

        BotApiMethod method = null;

        if (update.hasMessage()) {
            method = updateProcessor.handleUpdate(update);
        }

        if (method != null) {
            try {
                sendApiMethod(method);

            } catch (Exception e) {
                log.error(UPDATE_ERROR_MESSAGE, e);
                sendUserErrorMessage(update.getMessage().getChatId());
            }
        }

        return method;
    }

    @SneakyThrows
    private void sendUserErrorMessage(Long userId) {
        sendApiMethod(SendMessage.builder()
                .chatId(userId)
                .text(ERROR_MESSAGE)
                .build());
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return "/update";
    }
}
