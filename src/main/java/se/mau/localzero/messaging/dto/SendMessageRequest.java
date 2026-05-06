package se.mau.localzero.messaging.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import se.mau.localzero.domain.User;


@Data
@NoArgsConstructor
public class SendMessageRequest {
    private String content;
    private User receiver;
}
