package se.mau.localzero.messaging.validator;

import lombok.Setter;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.Notification;

public abstract class Validator {

    @Setter
    protected Validator next;

    public abstract boolean validate(Message message);

    public abstract boolean validate(Notification notification);

    public boolean checkAndPassMessageToNext(Message message) {
        if (!validate(message)) {
            return false;
        }

        if (next != null) {
            return next.checkAndPassMessageToNext(message);
        }

        return true;
    }

    public boolean checkAndPassNotificationToNext(Notification notification) {
        if (!validate(notification)) {
            return false;
        }

        if (next != null) {
            return next.checkAndPassNotificationToNext(notification);
        }
        return true;
    }
}
