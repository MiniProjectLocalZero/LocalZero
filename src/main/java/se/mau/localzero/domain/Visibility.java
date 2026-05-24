package se.mau.localzero.domain;

public enum Visibility {
    PUBLIC("Public"),
    COMMUNITY_ONLY("Community Only");

    private final String displayName;

    Visibility(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
