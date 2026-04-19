package se.mau.localzero.messaging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.command.MessageCommandInvoker;
import se.mau.localzero.messaging.command.SendMessageCommand;
import se.mau.localzero.messaging.exception.InvalidMessageException;
import se.mau.localzero.messaging.exception.UnauthorizedCrossCommunityCommunicationException;
import se.mau.localzero.messaging.repository.MessageRepository;
import se.mau.localzero.messaging.validator.ValidationChain;
import se.mau.localzero.repository.InitiativeRepository;

import java.util.Set;

@Service
public class DefaultCommunityMessagingMediator implements CommunityMessagingMediator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommunityMessagingMediator.class);

    private final MessageRepository messageRepository;
    private final NotificationService notificationService;
    private final InitiativeRepository initiativeRepository;
    private final MessageCommandInvoker messageCommandInvoker;
    private final ValidationChain validationChain;

    public DefaultCommunityMessagingMediator(
            MessageRepository messageRepository,
            NotificationService notificationService,
            InitiativeRepository initiativeRepository,
            MessageCommandInvoker messageCommandInvoker,
            ValidationChain validationChain
    ) {
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
        this.initiativeRepository = initiativeRepository;
        this.messageCommandInvoker = messageCommandInvoker;
        this.validationChain = validationChain;
    }

    @Override
    public boolean canSendMessage(User sender, User receiver) {
        // Sender cannot message themselves
        if (sender.equals(receiver)) {
            logger.warn("User {} attempted to message themselves", sender.getUsername());
            return false;
        }

        if (sender.getCommunity().equals(receiver.getCommunity())) {
            logger.debug("{} can message {} (same community)", sender.getUsername(), receiver.getUsername());
            return true;
        }

        Set<Initiative> sharedInitiatives = initiativeRepository.findSharedInitiatives(sender, receiver);
        boolean canCommunicate = !sharedInitiatives.isEmpty();

        if (!canCommunicate) {
            logger.warn("{} attempted cross-community message to {} without shared initiative", sender.getUsername(), receiver.getUsername());
        } else {
            logger.debug("{} can message {} via shared initiatives", sender.getUsername(), receiver.getUsername());
        }

        return canCommunicate;
    }

    @Override
    @Transactional
    public Message sendMessage(User sender, User receiver, String content) {
        logger.info("Starting message workflow: {} → {}", sender.getUsername(), receiver.getUsername());

        logger.debug("Step 1: Validating community rules");
        if (!canSendMessage(sender, receiver)) {
            throw new UnauthorizedCrossCommunityCommunicationException(
                    "User " + sender.getUsername() + " cannot send message to " + receiver.getUsername() +
                            ". Users must be in the same community or share an initiative."
            );
        }
        logger.debug("Step 1: Community rules validated ✓");

        logger.debug("Step 2: Validating message content");
        Message tempMessage = new Message(content, sender, receiver);
        if (!validationChain.validateMessage(tempMessage)) {
            throw new InvalidMessageException(
                    "Message validation failed for content: " + content
            );
        }
        logger.debug("Step 2: Message content validated ✓");

        logger.debug("Step 3: Executing SendMessageCommand");
        SendMessageCommand command = new SendMessageCommand(
                sender,
                receiver,
                content,
                messageRepository
        );

        boolean commandExecuted = messageCommandInvoker.execute(command);
        if (!commandExecuted) {
            throw new RuntimeException("Failed to execute SendMessageCommand");
        }

        Message createdMessage = command.getCreatedMessage();
        if (createdMessage == null) {
            throw new RuntimeException("Message was not created by command");
        }
        logger.debug("Step 3: Message created and saved ✓");

        logger.debug("Step 4: Creating notification");
        boolean isCrossCommunity = !sender.getCommunity().equals(receiver.getCommunity());
        notificationService.notifyNewMessage(sender, receiver, content, isCrossCommunity);
        logger.debug("Step 4: Notification created ✓");

        logger.info("Message workflow completed successfully: {} → {}", sender.getUsername(), receiver.getUsername());
        return createdMessage;
    }

    @Override
    public boolean requiresRepresentative(User sender, User receiver) {
        // Returns true if sender and receiver are in different communities
        return !sender.getCommunity().equals(receiver.getCommunity());
    }
}
