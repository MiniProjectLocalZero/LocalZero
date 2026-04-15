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

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"sender", "receiver"})
@Table(name = "message")
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

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
