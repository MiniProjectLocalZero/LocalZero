package se.mau.localzero.initiative.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.Post;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.PostDto;
import se.mau.localzero.initiative.factory.PostFactory;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.initiative.repository.PostRepository;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final InitiativeRepository initiativeRepository;
    private final PostFactory postFactory;

    public PostService(PostRepository postRepository, InitiativeRepository initiativeRepository, PostFactory postFactory) {
        this.postRepository = postRepository;
        this.initiativeRepository = initiativeRepository;
        this.postFactory = postFactory;
    }

    @Transactional
    public Post createPost(PostDto dto, User author, Long initiativeId) {
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        if (!initiative.getCreatedBy().getId().equals(author.getId())) {
            throw new RuntimeException("Only the initiative owner can post updates");
        }

        Post post = postFactory.createPost(dto, author, initiative);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<Post> getPostsByInitiative(Long initiativeId) {
        return postRepository.findByInitiativeIdOrderByCreatedAtDesc(initiativeId);
    }

    @Transactional(readOnly = true)
    public byte[] getImageData(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getImageData();
    }

    @Transactional(readOnly = true)
    public String getImageContentType(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return post.getImageContentType();
    }
}
