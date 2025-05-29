package school.sorokin.event.manager.telegrambot.telegram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import school.sorokin.event.manager.telegrambot.telegram.TelegramBot;

@Configuration
public class TelegramConfig {

    @Bean
    @Primary
    public DefaultAbsSender defaultAbsSender(TelegramBot telegramBot) {
        return telegramBot;
    }
}