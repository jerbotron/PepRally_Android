package com.peprally.jeremy.peprally.model;

public class NewNotificationResponse extends BaseResponse {

    boolean hasNewNotification;
    boolean hasNewMessage;

    public boolean hasNewNotification() {
        return hasNewNotification;
    }

    public boolean hasNewMessage() {
        return hasNewMessage;
    }
}
