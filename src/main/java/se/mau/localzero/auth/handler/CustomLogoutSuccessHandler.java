package se.mau.localzero.auth.handler;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import se.mau.localzero.auth.observer.EventType;
import se.mau.localzero.auth.observer.SessionEvent;
import se.mau.localzero.auth.observer.SessionSubject;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final SessionSubject logoutSubject;

    public CustomLogoutSuccessHandler(SessionSubject logoutSubject) {
        this.logoutSubject = logoutSubject;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            String username = authentication.getName();
            logoutSubject.notifyAllObservers(new SessionEvent(username, EventType.LOGOUT));
        }
        response.sendRedirect("/auth/login?logout");
    }
}
