package school.sorokin.event.manager.telegrambot.telegram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.GetFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

@Slf4j
@Service
public class TelegramFileService extends DefaultAbsSender {

    private static final String url = "https://api.telegram.org/file/bot%s/%s";

    public TelegramFileService(
            @Value("${bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }

    public byte[] downloadFileBytes(String fileId) {
        try {
            // Запрашиваем у Telegram информацию о файле
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(fileId);

            // Using DefaultAbsSender to execute the request to Telegram
            org.telegram.telegrambots.meta.api.objects.File tgFile = execute(getFileMethod);
            String filePath = tgFile.getFilePath();

            // Формируем URL для скачивания
            String downloadUrl = String.format(
                    url,
                    getBotToken(),
                    filePath);

            // Скачиваем файл
            try (InputStream is = new URL(downloadUrl).openStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.error("Failed to download file from Telegram. fileId={}", fileId, e);
            return new byte[0];
        }
    }
}