package school.sorokin.event.manager.telegrambot;

import org.telegram.telegrambots.meta.api.objects.Message;

public class Utils {
    private static final String AT = "@";

    public static String getAuthor(Message message) {

        if (message.getForwardFrom() != null) {
            return message.getForwardFrom().getUserName() != null
                    ? AT + message.getForwardFrom().getUserName()
                    : message.getForwardFrom().getFirstName();
        }

        return message.getFrom().getUserName() != null
                ? AT + message.getFrom().getUserName()
                : message.getFrom().getFirstName();
    }
}
