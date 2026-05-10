package se.mau.localzero.messaging.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.command.DeleteMessageCommand;
import se.mau.localzero.messaging.command.MessageCommand;
import se.mau.localzero.messaging.command.MessageCommandInvoker;
import se.mau.localzero.messaging.exception.MessageNotFoundException;
import se.mau.localzero.messaging.exception.UnauthorizedMessageAccessException;
import se.mau.localzero.messaging.mediator.CommunityMessagingMediator;
import se.mau.localzero.messaging.repository.MessageRepository;

import java.util.ArrayList;
import java.util.HashSet;
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

    public MessageService(
            CommunityMessagingMediator mediator,
            MessageRepository messageRepository,
            MessageCommandInvoker messageCommandInvoker
    ) {
        this.mediator = mediator;
        this.messageRepository = messageRepository;
        this.messageCommandInvoker = messageCommandInvoker;
    }

    /**
     * Delete a message. Soft deletion is used to preserve message history.
     * @param messageId The message to delete
     * @param currentUser The user attempting to delete the message (must be sender or receiver)
     * @throws MessageNotFoundException if message not found
     * @throws UnauthorizedMessageAccessException if user is not sender or receiver
     */
    @Transactional(readOnly = false)
    public void deleteMessage(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with ID: " + messageId));

        if (!message.getSender().equals(currentUser) && !message.getReceiver().equals(currentUser)) {
            throw new UnauthorizedMessageAccessException("Unauthorized: You can only delete messages you sent or received");
        }

        MessageCommand command = new DeleteMessageCommand(message, messageRepository);
        messageCommandInvoker.execute(command);
    }

    /**
     * Mark a message as unread.
     * @param messageId The message to mark as unread
     * @param currentUser The user attempting to mark the message
     * @throws MessageNotFoundException if message not found
     * @throws UnauthorizedMessageAccessException if user is not the receiver
     */
    @Transactional(readOnly = false)
    public void markMessageAsUnread(Long messageId, User currentUser) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Message not found with ID: " + messageId));

        if (!message.getReceiver().equals(currentUser)) {
            throw new UnauthorizedMessageAccessException("Unauthorized: You can only mark your own received messages as unread");
        }

        message.markAsUnread();
        messageRepository.save(message);
    }

    /**
     * Send a message from sender to receiver.
     * Delegates to the mediator which orchestrates validation, command execution, and notification.
     *
     * @param sender The user sending the message
     * @param receiver The user receiving the message
     * @param message The message content
     */
    @Transactional(readOnly = false)
    public void sendMessage(User sender, User receiver, String message) {
        mediator.sendMessage(sender, receiver, message);
    }

    /**
     * Get the inbox for a user.
     * @param currentUser The user to get the inbox for
     * @return A list of messages sent to the user, sorted by creation date (newest first)
     */
    public List<Message> getInbox(User currentUser) {
        return messageRepository.findByReceiverIdOrderByCreatedAtDesc(currentUser.getId()).orElse(List.of());
    }

    /**
     * Get the conversation between two users.
     * @param currentUser The user initiating the conversation
     * @param otherUser The user participating in the conversation
     * @return A list of messages in the conversation, sorted by creation date (oldest first)
     */
    public List<Message> getConversation(User currentUser, User otherUser) {
        return messageRepository.findConversationBetween(currentUser.getId(), otherUser.getId()).orElse(List.of());
    }

    /**
     * Get the count of unread messages for a user.
     * @param currentUser The user to get the count for
     * @return The number of unread messages
     */
    public int getUnreadCount(User currentUser) {
        return messageRepository.findByReceiverIdAndReadAtIsNull(currentUser.getId())
                .map(List::size)
                .orElse(0);
    }

    /**
     * Get the most recent received message from each unique sender.
     * Used for displaying an inbox overview.
     *
     * @param currentUser The user to get the inbox for
     * @return A list of unique messages from different senders
     */
    public List<Message> getRecentConversations(User currentUser) {
        List<Message> inbox = getInbox(currentUser);
        List<Message> previews = new ArrayList<>();
        Set<Long> seenSenderIds = new HashSet<>();

        for (Message message : inbox) {
            Long senderId = message.getSender().getId();
            if (seenSenderIds.add(senderId)) {
                previews.add(message);
            }
        }

        return previews;
    }

    /**
     * Mark all unread messages in a conversation as read.
     *
     * @param currentUser The user receiving the messages
     * @param otherUser The user who sent the messages
     */
    @Transactional(readOnly = false)
    public void markConversationAsRead(User currentUser, User otherUser) {
        List<Message> conversation = getConversation(currentUser, otherUser);
        boolean changed = false;
        for (Message msg : conversation) {
            if (msg.getReceiver().getId().equals(currentUser.getId()) && !msg.isRead()) {
                msg.markAsRead();
                changed = true;
            }
        }
        if (changed) {
            messageRepository.saveAll(conversation);
        }
    }
}
