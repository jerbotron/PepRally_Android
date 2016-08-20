package com.peprally.jeremy.peprally.enums;

public enum  IntentRequestEnum {
    INVALID_REQUEST(-1),
    FAV_TEAM_REQUEST(0),
    FAV_PLAYER_REQUEST(1),
    NEW_POST_REQUEST(2),
    POST_COMMENT_REQUEST(3),
    SETTINGS_REQUEST(4);

    private int value;

    private IntentRequestEnum(int value) { this.value = value; }

    public int toInt() {
        return value;
    }

    public static IntentRequestEnum fromInt(int request) {
        switch (request) {
            case 0:
                return FAV_TEAM_REQUEST;
            case 1:
                return FAV_PLAYER_REQUEST;
            case 2:
                return NEW_POST_REQUEST;
            case 3:
                return POST_COMMENT_REQUEST;
            case 4:
                return SETTINGS_REQUEST;
            default:
                return INVALID_REQUEST;
        }
    }
}
