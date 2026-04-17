package se.mau.localzero.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user account in LocalZero.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {
        "community",
        "participatingInitiatives",
        "createdInitiatives",
        "posts",
        "comments",
        "likes",
        "sustainabilityActions",
        "notifications",
        "sentMessages",
        "receivedMessages"
})
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    private Set<UserRole> roles = new HashSet<>();

    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    private Set<Initiative> participatingInitiatives = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private Set<Initiative> createdInitiatives = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<SustainabilityAction> sustainabilityActions = new HashSet<>();

    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private Set<Message> sentMessages = new HashSet<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY)
    private Set<Message> receivedMessages = new HashSet<>();

    public User(String username, String email, Community community, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        setCommunity(community);
    }

    public void setCommunity(Community community) {
        if (this.community != null) {
            this.community.getMembers().remove(this);
        }
        this.community = community;
        if (community != null) {
            community.getMembers().add(this);
        }
    }

    public void joinInitiative(Initiative initiative) {
        participatingInitiatives.add(initiative);
        initiative.getParticipants().add(this);
    }

    public void leaveInitiative(Initiative initiative) {
        participatingInitiatives.remove(initiative);
        initiative.getParticipants().remove(this);
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
