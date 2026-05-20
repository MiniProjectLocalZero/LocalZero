package se.mau.localzero.initiative.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.mau.localzero.initiative.service.PostService;

@Controller
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/{id}/image")
    public ResponseEntity<byte[]> getPostImage(@PathVariable Long id) {
        byte[] imageData = postService.getImageData(id);
        String contentType = postService.getImageContentType(id);

        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", contentType != null ? contentType : "application/octet-stream");
        return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
    }
}
