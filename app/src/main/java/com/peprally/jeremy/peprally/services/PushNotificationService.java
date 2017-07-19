package com.peprally.jeremy.peprally.services;

import retrofit2.Response;
import retrofit2.http.GET;

public interface PushNotificationService {

    @GET("/push")
    public Response makePushNotification(String receiverId);
}
