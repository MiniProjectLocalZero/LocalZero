package se.mau.localzero.messaging.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.command.MessageCommandInvoker;
import se.mau.localzero.messaging.command.SendMessageCommand;
import se.mau.localzero.messaging.exception.InvalidMessageException;
import se.mau.localzero.messaging.repository.MessageRepository;
import se.mau.localzero.messaging.validator.ValidationChain;

/**
 * Default implementation of CommunityMessagingMediator.
 * Orchestrates the workflow for sending messages between users, including validation of community rules, message content, and notification creation.
 */
@Service
public class DefaultCommunityMessagingMediator implements CommunityMessagingMediator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCommunityMessagingMediator.class);

    private final MessageRepository messageRepository;
    private final NotificationMediator notificationMediator;
    private final MessageCommandInvoker messageCommandInvoker;
    private final ValidationChain validationChain;

    public DefaultCommunityMessagingMediator(
            MessageRepository messageRepository,
            NotificationMediator notificationMediator,
            MessageCommandInvoker messageCommandInvoker,
            ValidationChain validationChain
    ) {
        this.messageRepository = messageRepository;
        this.notificationMediator = notificationMediator;
        this.messageCommandInvoker = messageCommandInvoker;
        this.validationChain = validationChain;
    }

    @Override
    @Transactional
    public boolean sendMessage(User sender, User receiver, String content) {
        logger.info("Starting message workflow: {} → {}", sender.getUsername(), receiver.getUsername());

        logger.debug("Step 2: Validating");
        Message tempMessage = new Message(content, sender, receiver);
        if (!validationChain.validateMessage(tempMessage)) {
            throw new InvalidMessageException(
                    "Message validation failed. \n Content: " +  content + "\n Sender: " + sender.getUsername() + "\n Receiver: " + receiver.getUsername()
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
            throw new InvalidMessageException("Failed to execute SendMessageCommand");
        }
        Message createdMessage = command.getCreatedMessage();
        logger.debug("Step 3: Message created and saved ✓");

        logger.debug("Step 4: Creating notification");
        boolean isCrossCommunity = !sender.getCommunity().equals(receiver.getCommunity());

        notificationMediator.sendMessageNotification(sender, receiver, createdMessage, isCrossCommunity);
        logger.debug("Step 4: Notification created ✓");

        logger.info("Message workflow completed successfully: {} → {}", sender.getUsername(), receiver.getUsername());
        return true;
    }
}
