package com.peprally.jeremy.peprally.enums;

public enum ActivityEnum {
    LOGIN("Login"),
    HOME("Home"),
    PROFILE("Profile"),
    SETTINGS("Settings"),
    FAVORITEPLAYER("FavoritePlayer"),
    FAVORITETEAM("FavoriteTeam"),
    NEWPOST("NewPost"),
    POSTCOMMENT("PostComment"),
    MESSAGING("Messaging"),
    CONVERSATIONS("Conversations"),
    NOTIFICATIONS("Notifications"),
    VIEWFISTBUMPS("ViewFistbumps");

    private final String name;
    ActivityEnum(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ActivityEnum fromString(String value) {
        for (ActivityEnum e : values()) {
            if (e.name.equals(value)) {
                return e;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
