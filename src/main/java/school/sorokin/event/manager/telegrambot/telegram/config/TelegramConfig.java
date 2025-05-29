package school.sorokin.event.manager.telegrambot.telegram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import school.sorokin.event.manager.telegrambot.telegram.TelegramBot;
import school.sorokin.event.manager.telegrambot.telegram.service.TelegramFileService;

@Configuration
public class TelegramConfig {

    @Bean("mainBotSender")
    @Primary
    public DefaultAbsSender mainBotSender(TelegramBot telegramBot) {
        return telegramBot;
    }

    @Bean("fileServiceSender")
    public DefaultAbsSender fileServiceSender(TelegramFileService telegramFileService) {
        return telegramFileService;
    }
}