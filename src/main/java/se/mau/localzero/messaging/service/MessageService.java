package se.mau.localzero.messaging.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.command.DeleteMessageCommand;
import se.mau.localzero.messaging.command.MessageCommand;
import se.mau.localzero.messaging.command.MessageCommandInvoker;
import se.mau.localzero.messaging.repository.MessageRepository;

import java.util.List;
import java.util.Set;

/**
 * Service class for handling message-related operations.
 */
@Service
@Transactional(readOnly = true)
public class MessageService {

    private final CommunityMessagingMediator mediator;
    private final MessageRepository messageRepository;
    private final MessageCommandInvoker messageCommandInvoker;

    public MessageService(CommunityMessagingMediator mediator, MessageRepository messageRepository, MessageCommandInvoker messageCommandInvoker) {
        this.mediator = mediator;
        this.messageRepository = messageRepository;
        this.messageCommandInvoker = messageCommandInvoker;
    }

    /**
     * Send a message from sender to receiver.
     * Delegates to the mediator which orchestrates validation, command execution, and notification.
     *
     * @param sender The user sending the message
     * @param receiver The user receiving the message
     * @param message The message content
     * @return The created Message entity
     */
    @Transactional
    public Message sendMessage(User sender, User receiver, String message) {
        return mediator.sendMessage(sender, receiver, message);
    }

    /**
     * Delete a message. Soft deletion is used to preserve message history.
     * @param message The message to delete
     * @return True if the message was deleted, false otherwise
     */
    @Transactional(readOnly = false)
    public boolean deleteMessage (Message message) {
        MessageCommand command = new DeleteMessageCommand(message, messageRepository);
        return messageCommandInvoker.execute(command);
    }

    /**
     * Mark a message as read.
     * @param message The message to mark as read
     * @return True if the message was found and updated, false otherwise
     */
    @Transactional(readOnly = false)
    public boolean markAsRead (Message message) {
        message.markAsRead();
        return messageRepository.save(message) != null;
    }

    /**
     * Mark a message as unread.
     * @param message The message to mark as unread
     * @return True if the message was found and updated, false otherwise
     */
    @Transactional(readOnly = false)
    public boolean markAsUnread (Message message) {
        message.markAsUnread();
        return messageRepository.save(message) != null;
    }

    public List<Message> getInbox(User currentUser) {
        Set<Message> inbox = currentUser.getReceivedMessages();
        return inbox.stream().toList();
    }

    public List<Message> getConversation(User currentUser, User otherUser) {
        return currentUser.getSentMessages().stream().filter(message -> message.getReceiver().equals(otherUser)).toList();
    }
}
