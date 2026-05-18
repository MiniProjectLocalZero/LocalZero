package se.mau.localzero.messaging.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import se.mau.localzero.CommunityRepository;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.UserRole;
import se.mau.localzero.messaging.repository.MessageRepository;
import se.mau.localzero.messaging.repository.NotificationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MessageControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        notificationRepository.deleteAll();
        messageRepository.deleteAll();
        userRepository.deleteAll();
        communityRepository.deleteAll();
    }

    @Test
    void sendMessage_sameCommunity_persistsMessageAndNotification() throws Exception {
        Community community = communityRepository.save(new Community("Eco Malmö"));
        User sender = userRepository.save(createUser("alice", "alice@example.com", community));
        User receiver = userRepository.save(createUser("bob", "bob@example.com", community));

        mockMvc.perform(post("/messages/send")
                        .param("fragment", "thread")
                        .param("receiver.id", receiver.getId().toString())
                        .param("content", "Hello from the integration test")
                        .with(userDetails(sender))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        List<Message> conversation = messageRepository.findConversationBetween(sender.getId(), receiver.getId())
                .orElseThrow();
        assertThat(conversation)
                .hasSize(1)
                .first()
                .extracting(Message::getContent)
                .isEqualTo("Hello from the integration test");

        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(receiver.getId())
                .orElseThrow();
        assertThat(notifications)
                .hasSize(1)
                .first()
                .extracting(Notification::getEntityId)
                .isEqualTo(conversation.getFirst().getId());
    }

    @Test
    void sendMessage_differentCommunitiesWithoutSharedInitiative_returnsForbidden() throws Exception {
        Community senderCommunity = communityRepository.save(new Community("Eco Malmö"));
        Community receiverCommunity = communityRepository.save(new Community("Eco Lund"));
        User sender = userRepository.save(createUser("charlie", "charlie@example.com", senderCommunity));
        User receiver = userRepository.save(createUser("diana", "diana@example.com", receiverCommunity));

        mockMvc.perform(post("/messages/send")
                        .param("fragment", "thread")
                        .param("receiver.id", receiver.getId().toString())
                        .param("content", "This should be blocked")
                        .with(userDetails(sender))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden cross-community communication"));

        assertThat(messageRepository.findConversationBetween(sender.getId(), receiver.getId()).orElse(List.of())).isEmpty();
        assertThat(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(receiver.getId()).orElse(List.of())).isEmpty();
    }

    @Test
    void sendMessage_missingReceiver_returnsNotFound() throws Exception {
        Community community = communityRepository.save(new Community("Eco Malmö"));
        User sender = userRepository.save(createUser("eve", "eve@example.com", community));

        mockMvc.perform(post("/messages/send")
                        .param("fragment", "thread")
                        .param("receiver.id", "999999")
                        .param("content", "This receiver does not exist")
                        .with(userDetails(sender))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));

        assertThat(messageRepository.findByReceiverIdOrderByCreatedAtDesc(sender.getId()).orElse(List.of())).isEmpty();
    }

    @Test
    void markAsUnread_marksMessageCorrectly() throws Exception {
        Community community = communityRepository.save(new Community("Eco Malmö"));
        User sender = userRepository.save(createUser("sender", "sender@example.com", community));
        User receiver = userRepository.save(createUser("receiver", "receiver@example.com", community));

        Message message = new Message("Hello", sender, receiver);
        message.markAsRead();
        message = messageRepository.save(message);
        assertThat(message.isRead()).isTrue();

        mockMvc.perform(post("/messages/" + message.getId() + "/unread")
                        .with(userDetails(receiver))
                        .with(csrf()))
                .andExpect(status().isOk());

        Message updatedMessage = messageRepository.findById(message.getId()).orElseThrow();
        assertThat(updatedMessage.isRead()).isFalse();
    }

    private User createUser(String username, String email, Community community) {
        User user = new User(username, email, community, "encoded-password");
        user.getRoles().add(UserRole.RESIDENT);
        return user;
    }

    private RequestPostProcessor userDetails(User user) {
        return SecurityMockMvcRequestPostProcessors.user(new LocalZeroUserDetails(user));
    }
}
