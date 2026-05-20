package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Comment;
import se.mau.localzero.domain.Post;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.CommentRepository;
import se.mau.localzero.initiative.repository.PostRepository;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public Comment addComment(Long postId, User author, String content) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("Comment content cannot be empty");
        }
        if (content.length() > 3000) {
            throw new RuntimeException("Comment content exceeds 3000 characters");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment(content, author, post);
        return commentRepository.save(comment);
    }
}
