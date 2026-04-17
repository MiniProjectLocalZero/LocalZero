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
@ToString(exclude = {"user", "post"})
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_likes_user_post", columnNames = {"user_id", "post_id"})
)
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    public Like(User user, Post post) {
        setUser(user);
        setPost(post);
    }

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getLikes().remove(this);
        }
        this.user = user;
        if (user != null) {
            user.getLikes().add(this);
        }
    }

    public void setPost(Post post) {
        if (this.post != null) {
            this.post.getLikes().remove(this);
        }
        this.post = post;
        if (post != null) {
            post.getLikes().add(this);
        }
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
