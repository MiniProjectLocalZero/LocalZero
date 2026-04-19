package se.mau.localzero.messaging.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.mau.localzero.messaging.dto.MessageResponse;
import se.mau.localzero.messaging.dto.SendMessageRequest;
import se.mau.localzero.messaging.service.MessageService;
import se.mau.localzero.messaging.service.CommunityMessagingMediator;

@Controller
public class MessageController {

    private MessageService messageService;
    private CommunityMessagingMediator mediator;

    public MessageController(MessageService messageService, CommunityMessagingMediator mediator) {
        this.messageService = messageService;
        this.mediator = mediator;
    }

    @PostMapping("/send")
    public MessageResponse sendMessage(@RequestBody SendMessageRequest request) {
        return null;
    }
}
