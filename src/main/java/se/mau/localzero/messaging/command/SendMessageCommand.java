package se.mau.localzero.messaging.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.repository.MessageRepository;

/**
 * Command to send a message from one user to another.
 * Encapsulates the creation and persistence of a new Message entity.
 */
public class SendMessageCommand implements MessageCommand {

    private static final Logger logger = LoggerFactory.getLogger(SendMessageCommand.class);

    private final User sender;
    private final User receiver;
    private final String content;
    private final MessageRepository messageRepository;

    private Message createdMessage;

    public SendMessageCommand(
            User sender,
            User receiver,
            String content,
            MessageRepository messageRepository
    ) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public boolean execute() {
        try {
            Message message = new Message(content, sender, receiver);
            createdMessage = messageRepository.save(message);
            logger.info("Message created from {} to {}", sender.getUsername(), receiver.getUsername());
            return true;
        } catch (Exception e) {
            logger.error("Failed to create and send message from {} to {}", sender.getUsername(), receiver.getUsername(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public void undo() {
        if (createdMessage != null) {
            createdMessage.markAsDeleted();
            messageRepository.save(createdMessage);
            logger.info("Message undo: {}", createdMessage.getId());
        }
    }

    @Override
    public String getDescription() {
        return "Send message from " + sender.getUsername() + " to " + receiver.getUsername();
    }

    /**
     * Retrieve the created message after execution.
     * Used by the mediator to get the persisted message entity.
     *
     * @return The created and persisted Message, or null if not yet executed
     */
    public Message getCreatedMessage() {
        return createdMessage;
    }
}
