package se.mau.localzero.messaging.validator;

import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Message;
import se.mau.localzero.messaging.exception.InvalidMessageException;
import se.mau.localzero.repository.UserRepository;

@Component
public class UserAccessValidator extends MessageValidator{

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
}
