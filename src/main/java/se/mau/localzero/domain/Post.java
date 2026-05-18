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
@ToString(exclude = {"author", "initiative", "comments", "likes"})
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiative_id")
    private Initiative initiative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private Set<Like> likes = new HashSet<>();

    public Post(String content, String imageUrl, User author, Initiative initiative) {
        this.content = content;
        this.imageUrl = imageUrl;
        setAuthor(author);
        setInitiative(initiative);
    }

    public void setAuthor(User author) {
        if (this.author != null) {
            this.author.getPosts().remove(this);
        }
        this.author = author;
        if (author != null) {
            author.getPosts().add(this);
        }
    }

    public void setInitiative(Initiative initiative) {
        if (this.initiative != null) {
            this.initiative.getPosts().remove(this);
        }
        this.initiative = initiative;
        if (initiative != null) {
            initiative.getPosts().add(this);
        }
    }

    public void setCommunity(Community community) {
        if (this.community != null) {
            this.community.removePost(this);
        }
        this.community = community;
        if (community != null) {
            community.addPost(this);
        }
    }

    /**
     * Validate that post is associated with either a community or initiative, but not both.
     * @throws IllegalStateException if both or neither community and initiative are set
     */
    public void validateScope() {
        boolean hasInitiative = this.initiative != null;
        boolean hasCommunity = this.community != null;

        if (hasInitiative && hasCommunity) {
            throw new IllegalStateException("Post cannot be in both an initiative and a community");
        }

        if (!hasInitiative && !hasCommunity) {
            throw new IllegalStateException("Post must be in either an initiative or a community");
        }
    }

    /**
     * Get the scope (container) of this post - either initiative or community.
     * @return the Initiative if post is in an initiative, or Community if post is in a community
     */
    public Object getScope() {
        return initiative != null ? initiative : community;
    }

    /**
     * Check if this post is in an initiative.
     * @return true if post is in an initiative, false if in a community
     */
    public boolean isInInitiative() {
        return initiative != null;
    }

    public void addComment(Comment comment) {
        if (comment != null && comments.add(comment)) {
            comment.setPost(this);
        }
    }

    public void removeComment(Comment comment) {
        if (comment != null && comments.remove(comment)) {
            comment.setPost(null);
        }
    }

    public void addLike(Like like) {
        if (like != null && likes.add(like)) {
            like.setPost(this);
        }
    }

    public void removeLike(Like like) {
        if (like != null && likes.remove(like)) {
            like.setPost(null);
        }
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
