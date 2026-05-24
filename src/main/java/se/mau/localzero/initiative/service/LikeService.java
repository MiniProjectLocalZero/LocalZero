package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Like;
import se.mau.localzero.domain.Post;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.repository.LikeRepository;
import se.mau.localzero.initiative.repository.PostRepository;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeService(LikeRepository likeRepository, PostRepository postRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public void toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        likeRepository.findByUserIdAndPostId(user.getId(), postId)
                .ifPresentOrElse(
                        likeRepository::delete,
                        () -> likeRepository.save(new Like(user, post))
                );
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikedPost(Long postId, Long userId) {
        return likeRepository.findByUserIdAndPostId(userId, postId).isPresent();
    }
}
