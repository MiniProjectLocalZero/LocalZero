package se.mau.localzero.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Represents a private message sent from one user to another within the LocalZero platform.
 * Each message has a sender, a receiver, content, and timestamps for when it was created and read.
 * <p>
 * The Message entity is designed to support a simple messaging system, allowing users to communicate privately.
 * It includes methods to mark messages as read or unread, and ensures that the relationships between users and messages are properly maintained.
 * <p>
 * Note: This implementation assumes that the User entity has corresponding collections for sent and received messages to maintain bidirectional relationships.
 */

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"sender", "receiver"})
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime deletedAt;

    @Column
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    public Message(String content, User sender, User receiver) {
        this.content = content;
        setSender(sender);
        setReceiver(receiver);
    }

    /**
     * Copy constructor for creating a deep copy of a message.
     * Used for preserving state before modifications (e.g., undo operations).
     * Creates a new instance with all fields copied from source.
     *
     * @param source The message to copy from
     */
    private Message(Message source) {
        this.id = source.id;
        this.content = source.content;
        this.createdAt = source.createdAt;
        this.readAt = source.readAt;
        this.deletedAt = source.deletedAt;
        this.sender = source.sender;
        this.receiver = source.receiver;
    }

    /**
     * Creates a deep copy of this message.
     * Used for undo operations to preserve the complete state of the message.
     *
     * @return A new Message instance with the same values
     */
    public Message copy() {
        return new Message(this);
    }

    public void setSender(User sender) {
        if (this.sender != null) {
            this.sender.getSentMessages().remove(this);
        }
        this.sender = sender;
        if (sender != null) {
            sender.getSentMessages().add(this);
        }
    }

    public void setReceiver(User receiver) {
        if (this.receiver != null) {
            this.receiver.getReceivedMessages().remove(this);
        }
        this.receiver = receiver;
        if (receiver != null) {
            receiver.getReceivedMessages().add(this);
        }
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    public void markAsUnread() {
        this.readAt = null;
    }

    public boolean isRead() {
        return this.readAt != null;
    }

    /**
     * Mark this message as deleted (soft delete).
     * Sets the deletedAt timestamp to the current time.
     * The message is not physically removed from the database.
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restore a soft-deleted message by clearing the deletedAt timestamp.
     * Used primarily for undo operations.
     */
    public void restore() {
        this.deletedAt = null;
    }

    /**
     * Check if this message has been soft-deleted.
     *
     * @return true if the message is deleted, false otherwise
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
