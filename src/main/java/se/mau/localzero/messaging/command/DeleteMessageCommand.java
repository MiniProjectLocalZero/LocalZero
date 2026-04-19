package se.mau.localzero.messaging.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Message;
import se.mau.localzero.messaging.repository.MessageRepository;
import se.mau.localzero.messaging.service.CommunityMessagingMediator;

public class DeleteMessageCommand implements MessageCommand {

    private static final Logger logger = LoggerFactory.getLogger(DeleteMessageCommand.class);

    private final Message message;
    private final MessageRepository messageRepository;

    private Message deletedMessageCopy;

    public DeleteMessageCommand(
            Message message,
            MessageRepository messageRepository
    ) {
        this.message = message;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public boolean execute() {
        try {
            deletedMessageCopy = message.copy();

            message.markAsDeleted();

            messageRepository.save(message);

            logger.info("Message deleted: {}", message.getId());
            return true;

        } catch (Exception e) {
            logger.error("Failed to delete message with ID: {}", message.getId(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public void undo() {
        try {
            if (deletedMessageCopy != null) {
                deletedMessageCopy.restore();
                messageRepository.save(deletedMessageCopy);
                logger.info("Message deletion undone: {}", deletedMessageCopy.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to undo delete message with ID: {}", message.getId(), e);
        }
    }

    @Override
    public String getDescription() {
        return "Delete message with ID: " + message.getId();
    }
}


