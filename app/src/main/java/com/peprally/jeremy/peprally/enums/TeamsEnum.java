package com.peprally.jeremy.peprally.enums;

public enum TeamsEnum {
    VOLLEYBALL("Volleyball", "ic_volleyball"),
    SWIMMING("Swimming and Diving", "ic_swimming"),
    ROWING("Rowing", "ic_rowing"),
    SOCCER("Soccer", "ic_soccer"),
    BASKETBALL("Basketball", "ic_basketball"),
    TENNIS("Tennis", "ic_tennis"),
    FOOTBALL("Football", "ic_football"),
    GOLF("Golf", "ic_golf"),
    SOFTBALL("Softball", "ic_softball"),
    TRACK("Track and Field", "ic_track");

    private final String name;
    private final String iconURI;
    TeamsEnum(String name, String iconURI) {
        this.name = name;
        this.iconURI = iconURI;
    }

    public String getName() { return name; }

    public String getIconURI() { return iconURI; }
}
