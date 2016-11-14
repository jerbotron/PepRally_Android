package com.peprally.jeremy.peprally.enums;

public enum  FeedbackEnum {
    GENERAL(0),
    ACCOUNT_DELETION(1);

    private int value;

    FeedbackEnum(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static FeedbackEnum fromInt(int x) {
        switch (x) {
            case 1:
                return ACCOUNT_DELETION;
            case 0:
            default:
                return GENERAL;
        }
    }
}
