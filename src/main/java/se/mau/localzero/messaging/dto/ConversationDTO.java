package se.mau.localzero.messaging.dto;

import lombok.Getter;
import lombok.Setter;
import se.mau.localzero.domain.User;

import java.util.List;

@Getter
@Setter
public class ConversationDTO {
    private User participant1;
    private User participant2;
    private List<MessageDTO> messages;
}
