package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Comment;
import se.mau.localzero.domain.CommentLike;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.CommentLikeRepository;
import se.mau.localzero.initiative.repository.CommentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CommentLikeService {
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    public CommentLikeService(CommentLikeRepository commentLikeRepository, CommentRepository commentRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void toggleLike(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        commentLikeRepository.findByUserIdAndCommentId(user.getId(), commentId)
                .ifPresentOrElse(
                        commentLikeRepository::delete,
                        () -> commentLikeRepository.save(new CommentLike(user, comment))
                );
    }

    @Transactional(readOnly = true)
    public long countLikes(Long commentId) {
        return commentLikeRepository.countByCommentId(commentId);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLiked(Long commentId, Long userId) {
        return commentLikeRepository.findByUserIdAndCommentId(userId, commentId).isPresent();
    }

    @Transactional(readOnly = true)
    public Set<Long> findLikedCommentIdsByUser(Long userId) {
        return commentLikeRepository.findLikedCommentIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> countLikesByCommentIds(Set<Long> commentIds) {
        List<Object[]> results = commentLikeRepository.countLikesByCommentIds(commentIds);
        Map<Long, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put((Long) result[0], (Long) result[1]);
        }
        for (Long id : commentIds) {
            map.putIfAbsent(id, 0L);
        }
        return map;
    }
}
