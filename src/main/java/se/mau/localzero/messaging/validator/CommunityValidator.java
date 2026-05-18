package se.mau.localzero.messaging.validator;

import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.messaging.exception.UnauthorizedCrossCommunityCommunicationException;

import java.util.Set;

@Component
public class CommunityValidator extends Validator {

    private final InitiativeRepository initiativeRepository;

    public CommunityValidator(InitiativeRepository initiativeRepository) {
        this.initiativeRepository = initiativeRepository;
    }

    @Override
    public boolean validate(Message message) {
        User sender = message.getSender();
        User receiver = message.getReceiver();


        if (sender.getCommunity().getId().equals(receiver.getCommunity().getId())) {
            return true;
        }

        Set<Initiative> sharedInitiatives = initiativeRepository.findSharedInitiatives(sender, receiver);
        if (sharedInitiatives.isEmpty()) {
            throw new UnauthorizedCrossCommunityCommunicationException(
                    sender.getUsername() + " and " + receiver.getUsername() +
                            " must be in the same community or share an initiative to message"
            );
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
