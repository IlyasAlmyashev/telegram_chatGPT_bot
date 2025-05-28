package school.sorokin.event.manager.telegrambot;

import org.telegram.telegrambots.meta.api.objects.Message;

import static school.sorokin.event.manager.telegrambot.Const.AT_SIGN;

public class Utils {

    public static String getAuthor(Message message) {

        if (message.getForwardFrom() != null) {
            return message.getForwardFrom().getUserName() != null
                    ? AT_SIGN + message.getForwardFrom().getUserName()
                    : message.getForwardFrom().getFirstName();
        }

        return message.getFrom().getUserName() != null
                ? AT_SIGN + message.getFrom().getUserName()
                : message.getFrom().getFirstName();
    }
}
