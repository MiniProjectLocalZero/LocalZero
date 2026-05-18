package se.mau.localzero.messaging.validator;

import org.springframework.stereotype.Component;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.messaging.exception.InvalidMessageException;

@Component
public class UserAccessValidator extends Validator {

    private UserRepository userRepository;

    public UserAccessValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean validate(Message message) {

        if(message.getSender() == null) {
            throw new InvalidMessageException("Sender cannot be null");
        }

        if (message.getReceiver() == null) {
            throw new InvalidMessageException("Receiver cannot be null");
        }

        if (!userRepository.existsById(message.getSender().getId())) {
            throw new InvalidMessageException("Sender does not exist");
        }

        if (!userRepository.existsById(message.getReceiver().getId())) {
            throw new InvalidMessageException("Receiver does not exist");
        }

        if (message.getSender().getId().equals(message.getReceiver().getId())) {
            throw new InvalidMessageException("Sender and receiver cannot be the same");
        }

        return true;
    }

    @Override
    public boolean validate(Notification notification) {

        if (notification.getRecipient() == null) {
            throw new InvalidMessageException("Receiver cannot be null");
        }

        if (!userRepository.existsById(notification.getRecipient().getId())) {
            throw new InvalidMessageException("Receiver does not exist");
        }

        return true;
    }
}
