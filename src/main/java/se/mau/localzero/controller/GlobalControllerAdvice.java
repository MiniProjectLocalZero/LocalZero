package se.mau.localzero.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

/**
 * This class is for loading for ALL pages, made for navbar as this is used on all pages
 * @ControllerAdvice - a Spring Boot function to automatically send data to all HTML pages loading
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    // This method will be called each time new page is loaded
    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        // Mockdata for now but will replace when we have real notifications :)

        model.addAttribute("unreadCount", 2); // How many unread notifs

        // Mockdata for testing
        List<Map<String, Object>> mockNotifications = List.of(
                Map.of("message", "Nini left a comment on your post", "timeAgo", "5 min ago", "isRead", false),
                Map.of("message", "You have earned 5 points this week", "timeAgo", "1 day ago", "isRead", true)
        );

        model.addAttribute("recentNotifications", mockNotifications);
    }
}