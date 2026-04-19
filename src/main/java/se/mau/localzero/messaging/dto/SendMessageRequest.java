package se.mau.localzero.messaging.dto;

import lombok.Getter;
import org.springframework.stereotype.Service;
import se.mau.localzero.domain.User;


@Getter
@Service
public class SendMessageRequest {
    private String content;
    private User recipient;
    private User sender;
}
