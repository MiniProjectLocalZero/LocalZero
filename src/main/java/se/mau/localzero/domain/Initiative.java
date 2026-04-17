package se.mau.localzero.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"createdBy", "community", "participants", "posts"})
@Table(name = "initiatives")
public class Initiative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "initiative_participants",
            joinColumns = @JoinColumn(name = "initiative_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "initiative", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    public Initiative(
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            Category category,
            Visibility visibility,
            User createdBy,
            Community community
    ) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.visibility = visibility;
        setCreatedBy(createdBy);
        setCommunity(community);
    }

    public void setCreatedBy(User createdBy) {
        if (this.createdBy != null) {
            this.createdBy.getCreatedInitiatives().remove(this);
        }
        this.createdBy = createdBy;
        if (createdBy != null) {
            createdBy.getCreatedInitiatives().add(this);
        }
    }

    public void setCommunity(Community community) {
        if (this.community != null) {
            this.community.getInitiatives().remove(this);
        }
        this.community = community;
        if (community != null) {
            community.getInitiatives().add(this);
        }
    }

    public void addParticipant(User user) {
        if (user != null && participants.add(user)) {
            user.getParticipatingInitiatives().add(this);
        }
    }

    public void removeParticipant(User user) {
        if (user != null && participants.remove(user)) {
            user.getParticipatingInitiatives().remove(this);
        }
    }

    public void addPost(Post post) {
        if (post != null && posts.add(post)) {
            post.setInitiative(this);
        }
    }

    public void removePost(Post post) {
        if (post != null && posts.remove(post)) {
            post.setInitiative(null);
        }
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
