package se.mau.localzero.initiative.factory;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.Post;
import se.mau.localzero.domain.User;
import se.mau.localzero.initiative.dto.PostDto;

import java.io.IOException;

@Component
public class PostFactory {

    public Post createPost(PostDto dto, User author, Initiative initiative) {
        byte[] imageData = null;
        String imageContentType = null;

        MultipartFile image = dto.getImage();
        if (image != null && !image.isEmpty()) {
            try {
                imageData = image.getBytes();
                imageContentType = image.getContentType();
            } catch (IOException e) {
                throw new RuntimeException("Failed to process image upload", e);
            }
        }

        return new Post(
                dto.getContent(),
                null,
                imageData,
                imageContentType,
                author,
                initiative
        );
    }
}
