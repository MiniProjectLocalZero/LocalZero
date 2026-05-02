package se.mau.localzero.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import se.mau.localzero.auth.observer.EventType;
import se.mau.localzero.auth.observer.SessionEvent;
import se.mau.localzero.auth.observer.SessionSubject;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final SessionSubject loginSubject;

    public CustomLoginSuccessHandler(SessionSubject loginSubject) {
        this.loginSubject = loginSubject;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        loginSubject.notifyAllObservers(new SessionEvent(username, EventType.LOGIN));
        response.sendRedirect("/");
    }
}
