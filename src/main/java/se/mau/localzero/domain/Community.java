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

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"members", "initiatives", "posts"})
@Table(name = "communities")
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY)
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY)
    private Set<Initiative> initiatives = new HashSet<>();

    @OneToMany(mappedBy = "community", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    public Community(String name) {
        this.name = name;
    }

    public void addMember(User user) {
        if (user != null && members.add(user)) {
            user.setCommunity(this);
        }
    }

    public void removeMember(User user) {
        if (user != null && members.remove(user)) {
            user.setCommunity(null);
        }
    }

    public void addInitiative(Initiative initiative) {
        if (initiative != null && initiatives.add(initiative)) {
            initiative.setCommunity(this);
        }
    }

    public void removeInitiative(Initiative initiative) {
        if (initiative != null && initiatives.remove(initiative)) {
            initiative.setCommunity(null);
        }
    }

    public void addPost(Post post) {
        if (post != null && posts.add(post)) {
            post.setCommunity(this);
        }
    }

    public void removePost(Post post) {
        if (post != null && posts.remove(post)) {
            post.setCommunity(null);
        }
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
