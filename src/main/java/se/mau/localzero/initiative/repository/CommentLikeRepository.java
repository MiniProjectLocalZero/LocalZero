package se.mau.localzero.initiative.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.CommentLike;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    long countByCommentId(Long commentId);

    @Query("SELECT cl.comment.id FROM CommentLike cl WHERE cl.user.id = :userId")
    Set<Long> findLikedCommentIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT cl.comment.id, COUNT(cl) FROM CommentLike cl WHERE cl.comment.id IN :ids GROUP BY cl.comment.id")
    List<Object[]> countLikesByCommentIds(@Param("ids") Set<Long> ids);
}
