package se.mau.localzero.messaging.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.auth.service.AuthService;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.dto.SendMessageRequest;
import se.mau.localzero.messaging.service.MessageService;

import java.util.List;

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
     * @return The name of the view to render
     */
    @GetMapping("/inbox")
    public String showInbox(@AuthenticationPrincipal LocalZeroUserDetails userDetails, Model model) {
        User currentUser = userDetails.getUser();

        List<Message> receivedMessages = messageService.getInbox(currentUser);
        model.addAttribute("messages", receivedMessages);
        model.addAttribute("unreadCount", messageService.getUnreadCount(currentUser));
        return "inbox";
    }

    /**
     * Shows a conversation with another user.
     * @param userDetails The currently authenticated user
     * @param userId The ID of the other user
     * @param model The Spring MVC model to pass data to the view
     * @return The name of the view to render
     */
    @GetMapping("/conversation/{userId}")
    public String showConversation(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                                   @PathVariable Long userId,
                                   Model model) {
        User currentUser = userDetails.getUser();
        User otherUser = authService.getUserById(userId);

        List<Message> conversation = messageService.getConversation(currentUser, otherUser);
        model.addAttribute("messages", conversation);
        model.addAttribute("otherUser", otherUser);
        return "conversation";
    }

    /**
     * Sends a new message to a user.
     * @param userDetails The currently authenticated user
     * @param request The SendMessageRequest containing the message content, sender and receiver
     * @return The name of the view to redirect to after sending the message
     */
    @PostMapping("/send")
    public String sendMessage(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                             @ModelAttribute SendMessageRequest request) {
        User sender = userDetails.getUser();
        User receiver = authService.getUserById(request.getReceiver().getId());

        messageService.sendMessage(sender, receiver, request.getContent());
        return "redirect:/messages/conversation/" + receiver.getId() + "?success";
    }

    /**
     * Marks a message as read.
     * @param userDetails The currently authenticated user
     * @param messageId The ID of the message to mark as read
     * @return The name of the view to redirect to after marking the message as read
     */
    @PostMapping("/{messageId}/read")
    public String markAsRead(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                            @PathVariable Long messageId) {
        User currentUser = userDetails.getUser();
        messageService.markMessageAsRead(messageId, currentUser);
        return "redirect:/messages/inbox?success";
    }

    /**
     * Marks a message as unread.
     * @param userDetails The currently authenticated user
     * @param messageId The ID of the message to mark as unread
     * @return The name of the view to redirect to after marking the message as unread
     */
    @PostMapping("/{messageId}/unread")
    public String markAsUnread(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                              @PathVariable Long messageId) {
        User currentUser = userDetails.getUser();
        messageService.markMessageAsUnread(messageId, currentUser);
        return "redirect:/messages/inbox?success";
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
        User currentUser = userDetails.getUser();
        messageService.deleteMessage(messageId, currentUser);
        return "redirect:/messages/inbox?success";
    }
}