package se.mau.localzero.messaging.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.auth.service.AuthService;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.dto.SendMessageRequest;
import se.mau.localzero.messaging.service.MessageService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MessageControllerTest {

    private TrackingMessageService messageService;
    private LookupAuthService authService;
    private MessageController messageController;
    private Model model;

    @BeforeEach
    void setUp() {
        messageService = new TrackingMessageService();
        authService = new LookupAuthService();
        messageController = new MessageController(messageService, authService);
        model = new ExtendedModelMap();
    }

    @Test
    void sendMessage_usesManagedUserForSender() {
        User detachedUser = createUser(1L, "sender-detached");
        User managedSender = createUser(1L, "sender-managed");
        User managedReceiver = createUser(2L, "receiver-managed");
        authService.sender = managedSender;
        authService.receiver = managedReceiver;

        SendMessageRequest request = new SendMessageRequest();
        User receiverStub = createUser(2L, "receiver-request");
        request.setReceiver(receiverStub);
        request.setContent("Hello");

        String view = messageController.sendMessage(new LocalZeroUserDetails(detachedUser), null, request, model);

        assertEquals("redirect:/messages/conversation/2", view);
        assertSame(managedSender, messageService.sentSender);
        assertSame(managedReceiver, messageService.sentReceiver);
        assertEquals("Hello", messageService.sentContent);
        assertEquals(2, authService.lookupCount);
    }

    private User createUser(Long id, String username) {
        User user = new User(username, username + "@example.com", new Community("Test Community"), "password");
        user.setId(id);
        return user;
    }

    private static final class TrackingMessageService extends MessageService {
        private User sentSender;
        private User sentReceiver;
        private String sentContent;

        private TrackingMessageService() {
            super(null, null, null);
        }

        @Override
        public void sendMessage(User sender, User receiver, String message) {
            this.sentSender = sender;
            this.sentReceiver = receiver;
            this.sentContent = message;
        }
    }

    private static final class LookupAuthService extends AuthService {
        private User sender;
        private User receiver;
        private int lookupCount;

        private LookupAuthService() {
            super(null, null, null);
        }

        @Override
        public User getUserById(Long userId) {
            lookupCount++;
            if (sender != null && sender.getId().equals(userId)) {
                return sender;
            }
            if (receiver != null && receiver.getId().equals(userId)) {
                return receiver;
            }
            throw new IllegalArgumentException("Unexpected user ID: " + userId);
        }
    }
}
