package se.mau.localzero.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import se.mau.localzero.auth.model.LocalZeroUserDetails;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.service.NotificationService;

import java.util.List;

/**
 * This class is for loading for ALL pages, made for navbar as this is used on all pages
 * @ControllerAdvice - a Spring Boot function to automatically send data to all HTML pages loading
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("recentNotifications")
    public List<Notification> addRecentNotifications(@AuthenticationPrincipal LocalZeroUserDetails userDetails) {
        if (userDetails != null) {
            User user = userDetails.getUser();
            List<Notification> allNotifs = notificationService.getNotificationInbox(user);

            if (allNotifs.size() > 5) {
                return allNotifs.subList(0, 5);
            }
            return allNotifs;
        }
        return List.of();
    }

    @ModelAttribute("unreadCount")
    public long addUnreadCount(@AuthenticationPrincipal LocalZeroUserDetails userDetails) {
        if (userDetails != null) {
            return notificationService.getUnreadNotificationCount(userDetails.getUser());
        }
        return 0;
    }
}