package com.peprally.jeremy.peprally.enums;

public enum  SchoolsSupportedEnum {
    PROMPT_TEXT("Choose your school and campus"),
    UT_AUSTIN("University of Texas - Austin"),
    TEXAS_STATE("Texas State University");

    private final String schoolName;

    SchoolsSupportedEnum(String schoolName) {
        this.schoolName = schoolName;
    }

    @Override
    public String toString() {
        return schoolName;
    }

    public static SchoolsSupportedEnum fromString(String value) {
        for (SchoolsSupportedEnum e : values()) {
            if (e.schoolName.equals(value)) {
                return e;
            }
        }
        return PROMPT_TEXT;
    }

    public int getIndex() {
        switch (fromString(schoolName)) {
            case UT_AUSTIN:
                return 1;
            case TEXAS_STATE:
                return 2;
            case PROMPT_TEXT:
            default:
                return 0;
        }
    }
}
