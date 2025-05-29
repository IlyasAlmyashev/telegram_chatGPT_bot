package school.sorokin.event.manager.telegrambot.openai.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageContent(
        String type, // Например: "document", "photo"
        String fileName,
        byte[] bytes,
        String mimeType) {
}