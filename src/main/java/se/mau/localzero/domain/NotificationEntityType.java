package se.mau.localzero.domain;

import lombok.Getter;

@Getter
public enum NotificationEntityType {
    MESSAGE("Message"),
    INITIATIVE("Initiative"),
    COMMUNITY("Community"),
    POST("Post"),
    COMMENT("Comment"),
    LIKE("Like"),
    OTHER("Other");

    private final String displayName;

    NotificationEntityType(String displayName) {
        this.displayName = displayName;
    }

}
