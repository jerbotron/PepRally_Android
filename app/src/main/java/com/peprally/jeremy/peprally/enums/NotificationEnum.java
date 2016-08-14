package com.peprally.jeremy.peprally.enums;

public enum NotificationEnum {
    INVALID(-1),
    DIRECT_FISTBUMP(0),
    DIRECT_MESSAGE(1),
    POST_COMMENT(2),
    POST_FISTBUMP(3),
    COMMENT_FISTBUMP(4);

    private int value;

    private NotificationEnum(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static NotificationEnum fromInt(int x) {
        switch (x) {
            case 0:
                return DIRECT_FISTBUMP;
            case 1:
                return DIRECT_MESSAGE;
            case 2:
                return POST_COMMENT;
            case 3:
                return POST_FISTBUMP;
            case 4:
                return COMMENT_FISTBUMP;
            default:
                return INVALID;
        }
    }
};
