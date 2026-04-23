package se.mau.localzero.messaging.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.service.NotificationService;

import java.util.List;

/**
 * Controller for handling notification-related HTTP requests.
 * Provides endpoints for viewing, marking as read, and deleting notifications.
 */
@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Display the notification inbox for the currently authenticated user.
     * Shows all unread notifications with most recent first.
     *
     * @param userDetails The currently authenticated user
     * @param model The model to pass data to the view
     * @return The name of the view to render
     */
    @GetMapping("/inbox")
    public String showInbox(@AuthenticationPrincipal LocalZeroUserDetails userDetails, Model model) {
        try {
            User currentUser = userDetails.getUser();

            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(currentUser);
            long unreadCount = notificationService.getUnreadNotificationCount(currentUser);

            model.addAttribute("unreadNotifications", unreadNotifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("notificationService", notificationService); // For link generation in template

            return "notifications/inbox";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load notifications: " + e.getMessage());
            return "notifications/inbox";
        }
    }

    /**
     * Get unread notification count for AJAX/navbar updates.
     * Returns JSON count.
     *
     * GET /notifications/unread-count
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public long getUnreadCount(@AuthenticationPrincipal LocalZeroUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        return notificationService.getUnreadNotificationCount(currentUser);
    }

    /**
     * Mark a single notification as read.
     *
     * POST /notifications/{id}/read
     */
    @PostMapping("/{id}/read")
    public String markAsRead(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                            @PathVariable("id") Long notificationId) {
        User currentUser = userDetails.getUser();
        notificationService.markAsRead(notificationId, currentUser);
        return "redirect:/notifications/inbox?success";
    }

    /**
     * Mark a single notification as unread (snooze feature).
     *
     * POST /notifications/{id}/unread
     */
    @PostMapping("/{id}/unread")
    public String markAsUnread(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                              @PathVariable("id") Long notificationId) {
        User currentUser = userDetails.getUser();
        notificationService.markAsUnread(notificationId, currentUser);
        return "redirect:/notifications/inbox?success";
    }

    /**
     * Delete a notification.
     *
     * POST /notifications/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String deleteNotification(@AuthenticationPrincipal LocalZeroUserDetails userDetails,
                                     @PathVariable("id") Long notificationId) {
        User currentUser = userDetails.getUser();
        notificationService.deleteNotification(notificationId, currentUser);
        return "redirect:/notifications/inbox?success";
    }
}
