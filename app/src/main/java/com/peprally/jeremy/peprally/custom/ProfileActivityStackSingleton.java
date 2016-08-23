package com.peprally.jeremy.peprally.custom;

import java.util.Stack;

public class ProfileActivityStackSingleton {

    private Stack<UserProfileParcel> userProfileParcelStack;

    private static ProfileActivityStackSingleton profileStackSingletonInstance = null;

    private ProfileActivityStackSingleton() {
        userProfileParcelStack = new Stack<>();
    }

    public static ProfileActivityStackSingleton getInstance() {
        if (profileStackSingletonInstance == null) {
            profileStackSingletonInstance = new ProfileActivityStackSingleton();
        }
        return profileStackSingletonInstance;
    }

    public void push(UserProfileParcel userProfileParcel) {
        userProfileParcelStack.push(userProfileParcel);
    }

    public UserProfileParcel pop() {
        return userProfileParcelStack.pop();
    }

    public UserProfileParcel peek() {
        return userProfileParcelStack.peek();
    }
}
