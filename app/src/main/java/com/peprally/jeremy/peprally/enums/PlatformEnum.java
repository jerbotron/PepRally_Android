package com.peprally.jeremy.peprally.enums;

public enum PlatformEnum {
    ANDROID(0),
    IOS(1);

    private int value;

    PlatformEnum(int value) { this.value = value; }

    public int toInt() { return value; }

    public static PlatformEnum fromInt(int x) {
        switch (x) {
            case 0:
                return ANDROID;
            case 1:
                return IOS;
        }
        return null;
    }
}
