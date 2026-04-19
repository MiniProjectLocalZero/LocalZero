package se.mau.localzero.messaging.dto;


import lombok.Getter;
import lombok.Setter;
import se.mau.localzero.domain.User;

@Getter
@Setter
public class MessageDTO {
    private String content;
    private User sender;
    private User recipient;
}
