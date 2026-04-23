package se.mau.localzero.domain;

import jakarta.persistence.*;
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
@ToString(exclude = "recipient")
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationEntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    public Notification(String title, String message, User recipient, NotificationEntityType entityType, Long entityId) {
        this.title = title;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
        setRecipient(recipient);
    }

    public void setRecipient(User recipient) {
        if (this.recipient != null) {
            this.recipient.getNotifications().remove(this);
        }
        this.recipient = recipient;
        if (recipient != null) {
            recipient.getNotifications().add(this);
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

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
