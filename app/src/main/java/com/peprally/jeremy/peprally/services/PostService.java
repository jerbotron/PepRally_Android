package com.peprally.jeremy.peprally.services;

import com.peprally.jeremy.peprally.model.BaseResponse;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PostService {

    @POST("/post/like")
    Call<BaseResponse> likePost(@Query("notification_type") int notificationType,
                                @Query("sender") String sender,
                                @Query("receiver") String receiver,
                                @Query("sender_fb_id") String senderFbId,
                                @Query("post_id") String postId);

}
