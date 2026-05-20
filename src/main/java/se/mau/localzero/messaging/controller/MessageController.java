package se.mau.localzero.messaging.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.auth.service.AuthService;
import se.mau.localzero.domain.Community;
import se.mau.localzero.domain.User;
import se.mau.localzero.domain.UserRole;
import se.mau.localzero.messaging.dto.SendMessageRequest;
import se.mau.localzero.messaging.dto.UserSummaryDTO;
import se.mau.localzero.messaging.service.MessageService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for handling message-related HTTP requests.
 * Provides endpoints for viewing inbox, sending messages, marking messages as read, and deleting messages.
 */
@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final AuthService authService;

    public MessageController(MessageService messageService, AuthService authService) {
        this.messageService = messageService;
        this.authService = authService;
    }

    /**
     * Shows the user's inbox with unread messages marked.
     * @param userDetails The currently authenticated user
     * @param model The Spring MVC model to pass data to the view
     * @param fragment Controls whether to return the inbox page or a Thymeleaf fragment
     * @return The name of the view to render
     */
    @GetMapping("/inbox")
    public String showInbox(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                            @RequestParam(value = "fragment", required = false) String fragment,
                            Model model) {
        User currentUser = getManagedUser(userDetails);
        populateInboxModel(model, currentUser);

        if (isFragment(fragment, "modal")) {
            return "fragments/messaging-modal :: content";
        }
        if (isFragment(fragment, "list")) {
            return "fragments/message-list :: content";
        }

        return "inbox";
    }

    /**
     * Shows a conversation with another user.
     * @param userDetails The currently authenticated user
     * @param userId The ID of the other user
     * @param model The Spring MVC model to pass data to the view
     * @param fragment Controls whether to return the conversation page or a Thymeleaf fragment
     * @return The name of the view to render
     */
    @GetMapping("/conversation/{userId}")
    public String showConversation(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                                   @RequestParam(value = "fragment", required = false) String fragment,
                                   @PathVariable Long userId,
                                   Model model) {
        User currentUser = getManagedUser(userDetails);
        User otherUser = authService.getUserById(userId);

        messageService.markConversationAsRead(currentUser, otherUser);
        populateConversationModel(model, currentUser, otherUser);

        if (isFragment(fragment, "thread")) {
            return "fragments/conversation-view :: content";
        }

        return "conversation";
    }

    /**
     * Sends a new message to a user.
     * @param userDetails The currently authenticated user
     * @param request The SendMessageRequest containing the message content, sender and receiver
     * @param fragment Controls whether to return the updated conversation fragment or redirect
     * @return The name of the view to redirect to after sending the message
     */
    @PostMapping("/send")
    public String sendMessage(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                              @RequestParam(value = "fragment", required = false) String fragment,
                              @ModelAttribute SendMessageRequest request,
                              Model model) {
        User sender = getManagedUser(userDetails);
        User receiver = authService.getUserById(request.getReceiver().getId());

        messageService.sendMessage(sender, receiver, request.getContent());

        if (isFragment(fragment, "thread")) {
            populateConversationModel(model, sender, receiver);
            return "fragments/conversation-view :: content";
        }

        return "redirect:/messages/conversation/" + receiver.getId();
    }

    /**
     * Deletes a message.
     * @param userDetails The currently authenticated user
     * @param messageId The ID of the message to delete
     * @return The name of the view to redirect to after deleting the message
     */
    @PostMapping("/{messageId}/delete")
    public String deleteMessage(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                               @PathVariable Long messageId) {
        User currentUser = getManagedUser(userDetails);
        messageService.deleteMessage(messageId, currentUser);
        return "redirect:/messages/inbox";
    }

    /**
     * Marks a message as unread.
     * Used by the AJAX call from the conversation list.
     *
     * @param userDetails The currently authenticated user
     * @param messageId The ID of the message to mark as unread
     * @return HTTP 200 OK on success
     */
    @PostMapping("/{messageId}/unread")
    public ResponseEntity<Void> markAsUnread(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                             @PathVariable Long messageId) {
        User currentUser = getManagedUser(userDetails);
        messageService.markMessageAsUnread(messageId, currentUser);
        return ResponseEntity.ok().build();
    }

    private void populateInboxModel(Model model, User currentUser) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("conversations", messageService.getRecentConversations(currentUser));
        model.addAttribute("unreadCount", messageService.getUnreadCount(currentUser));
        model.addAttribute("isRepresentative", currentUser.getRoles().contains(UserRole.REPRESENTATIVE));
    }

    private void populateConversationModel(Model model, User currentUser, User otherUser) {
        populateInboxModel(model, currentUser);
        List<se.mau.localzero.domain.Message> messages = messageService.getConversation(currentUser, otherUser);
        model.addAttribute("messages", messages);
        model.addAttribute("otherUser", otherUser);
        model.addAttribute("activeConversationUserId", otherUser.getId());
        
        // Check if conversation should be read-only (e.g. contains broadcast messages)
        boolean hasBroadcast = messages.stream().anyMatch(se.mau.localzero.domain.Message::isBroadcast);
        model.addAttribute("isReadOnly", hasBroadcast);
    }

    private boolean isFragment(String fragment, String expected) {
        return expected.equalsIgnoreCase(fragment);
    }

    /**
     * Get a managed user from the security principal.
     * Re-fetches from DB to avoid lazy-loading issues on detached entities.
     *
     * @param userDetails The security principal
     * @return A managed User entity
     */
    private User getManagedUser(LocalZeroUserDetails userDetails) {
        return authService.getUserById(userDetails.getUser().getId());
    }

    /**
     * Get list of all users in the current user's community (excluding self).
     * If the user is a REPRESENTATIVE, also include all other REPRESENTATIVEs from other communities.
     *
     * @param userDetails The currently authenticated user
     * @return JSON list of users as UserSummaryDTOs
     */
    @GetMapping("/community-users")
    @ResponseBody
    public List<UserSummaryDTO> getCommunityUsers(@AuthenticationPrincipal LocalZeroUserDetails userDetails) {
        User managedUser = getManagedUser(userDetails);
        Community community = managedUser.getCommunity();

        Set<UserSummaryDTO> userSummaries = new HashSet<>();

        // Add users from the same community
        if (community != null) {
            community.getMembers().stream()
                    .filter(u -> !u.getId().equals(managedUser.getId()))
                    .forEach(u -> userSummaries.add(new UserSummaryDTO(
                            u.getId(), 
                            u.getUsername(), 
                            community.getName(),
                            u.getRoles().contains(UserRole.REPRESENTATIVE)
                    )));
        }

        // If user is a REPRESENTATIVE, add all other REPRESENTATIVEs
        if (managedUser.getRoles().contains(UserRole.REPRESENTATIVE)) {
            authService.getAllRepresentatives().stream()
                    .filter(u -> !u.getId().equals(managedUser.getId()))
                    .forEach(u -> userSummaries.add(new UserSummaryDTO(
                            u.getId(), 
                            u.getUsername(), 
                            u.getCommunity().getName(),
                            true
                    )));
        }

        return userSummaries.stream().toList();
    }

    /**
     * Broadcasts a message to all community members.
     * Only available to REPRESENTATIVEs.
     *
     * @param userDetails The currently authenticated user
     * @param content The message content
     * @return HTTP 200 OK on success
     */
    @PostMapping("/broadcast")
    @ResponseBody
    public ResponseEntity<?> broadcastMessage(
            @AuthenticationPrincipal LocalZeroUserDetails userDetails,
            @RequestParam String content) {

        User sender = getManagedUser(userDetails);

        if (!sender.getRoles().contains(UserRole.REPRESENTATIVE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only representatives can broadcast messages"));
        }

        try {
            messageService.broadcastMessage(sender, content);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send broadcast: " + e.getMessage()));
        }
    }
}
