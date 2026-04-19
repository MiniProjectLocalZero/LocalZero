package se.mau.localzero.messaging.validator;


import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Message;
import se.mau.localzero.messaging.exception.InvalidMessageException;

@Component
public class ContentValidator extends MessageValidator{

    @Override
    public boolean validate(Message message) {
        String content = message.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidMessageException("Message content cannot be empty");
        }

        if (content.length() > 5000) {
            throw new InvalidMessageException("Message content cannot be longer than 5000 characters");
        }

        return true;
    }
}
