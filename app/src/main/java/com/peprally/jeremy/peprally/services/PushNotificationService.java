package com.peprally.jeremy.peprally.services;

import com.peprally.jeremy.peprally.model.PostLike;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface PushNotificationService {

    @FormUrlEncoded
    @POST("/push")
    Call<PostLike> likePost(@Field("sender") String sender,
                            @Field("receiver") String receiver);

}
