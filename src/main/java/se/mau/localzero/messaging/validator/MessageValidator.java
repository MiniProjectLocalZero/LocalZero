package se.mau.localzero.messaging.validator;

import lombok.Setter;
import se.mau.localzero.domain.Message;

public abstract class MessageValidator {

    @Setter
    protected MessageValidator next;

    public abstract boolean validate(Message message);

    public boolean checkAndPassToNext(Message message) {
        if (!validate(message)) {
            return false;
        }

        if (next != null) {
            return next.checkAndPassToNext(message);
        }

        return true;
    }
}
