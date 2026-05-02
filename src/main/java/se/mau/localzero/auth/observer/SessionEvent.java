package se.mau.localzero.auth.observer;

import lombok.Getter;

@Getter
public class SessionEvent {
    private String username;
    private EventType eventType;

    public SessionEvent(String username, EventType eventType) {
        this.username = username;
        this.eventType = eventType;
    }
}
