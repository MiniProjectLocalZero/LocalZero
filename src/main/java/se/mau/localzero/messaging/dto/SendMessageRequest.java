package se.mau.localzero.messaging.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import se.mau.localzero.domain.User;


@Data
@NoArgsConstructor
public class SendMessageRequest {
    private String content;
    private User receiver;
}
