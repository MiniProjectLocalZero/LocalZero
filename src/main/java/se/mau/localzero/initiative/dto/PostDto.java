package se.mau.localzero.initiative.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostDto {
    private String content;
    private MultipartFile image;
}
