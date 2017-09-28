package com.peprally.jeremy.peprally.services;

import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.model.BaseResponse;
import com.peprally.jeremy.peprally.model.NewNotificationResponse;
import com.peprally.jeremy.peprally.model.PostDeletionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NotificationService {

    @GET("/notification/new_notification")
    Call<NewNotificationResponse> checkNewNotifications(@Query("username") String username);

    @GET("/notification/delete_notification")
    Call<PostDeletionResponse> deleteNotification(@Query("post_id") String postId,
                                                  @Query("comment_id") String commentId);

    @GET("/notification/create_comment_fistbump_notification")
    Call<BaseResponse> createNewNotification(@Query("comment") Comment comment);
}
