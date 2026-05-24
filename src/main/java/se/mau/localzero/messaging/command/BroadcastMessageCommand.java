package se.mau.localzero.messaging.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.UserRole;
import se.mau.localzero.messaging.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to support community-wide messaging by Representatives.
 * This allows a Community Representative to easily reach all residents in their neighborhood.
 */
public class BroadcastMessageCommand implements MessageCommand {

    private static final Logger logger = LoggerFactory.getLogger(BroadcastMessageCommand.class);

    private final User sender;
    private final Community community;
    private final String content;
    private final MessageRepository messageRepository;

    private final List<Message> createdMessages = new ArrayList<>();

    /**
     * Constructor for BroadcastMessageCommand.
     *
     * @param sender            The user sending the broadcast
     * @param community         The community to receive the broadcast
     * @param content           The message content
     * @param messageRepository The repository for persisting messages
     */
    public BroadcastMessageCommand(
            User sender,
            Community community,
            String content,
            MessageRepository messageRepository
    ) {
        this.sender = sender;
        this.community = community;
        this.content = content;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public boolean execute() {
        if (!sender.getRoles().contains(UserRole.REPRESENTATIVE)) {
            logger.warn("User {} attempted to broadcast a message but lacks REPRESENTATIVE role", sender.getUsername());
            return false;
        }

        for (User member : community.getMembers()) {
            if (!member.equals(sender)) {
                Message message = new Message(content, sender, member);
                message.setBroadcast(true);
                createdMessages.add(messageRepository.save(message));
            }
        }

        logger.info("Broadcast message from {} to {} members in community {}",
                sender.getUsername(), createdMessages.size(), community.getName());
        return true;
    }

    @Override
    @Transactional
    public void undo() {
        for (Message message : createdMessages) {
            message.markAsDeleted();
            messageRepository.save(message);
        }
        logger.info("Broadcast message undo: {} messages marked as deleted", createdMessages.size());
    }

    @Override
    public String getDescription() {
        return "Broadcast message from " + sender.getUsername() + " to all members of " + community.getName();
    }

    /**
     * Retrieve the list of created messages after execution.
     *
     * @return The list of created messages
     */
    public List<Message> getCreatedMessages() {
        return createdMessages;
    }
}
