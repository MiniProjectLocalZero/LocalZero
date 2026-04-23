package se.mau.localzero.messaging.validator;

import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.messaging.exception.InvalidMessageException;

@Component
public class CommunityValidator extends Validator {

    @Override
    public boolean validate(Message message) {
        if (!message.getSender().getCommunity().getId()
            .equals(message.getReceiver().getCommunity().getId())) {
            throw new InvalidMessageException("Users must be in the same community to message");
        }

        return true;
    }

    @Override
    public boolean validate(Notification notification) {
        // Notifications don't require community validation
        // The recipient's community is implicit in their user account
        return true;
    }
}
