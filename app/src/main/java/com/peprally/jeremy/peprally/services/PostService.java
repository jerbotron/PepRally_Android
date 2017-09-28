package com.peprally.jeremy.peprally.services;

import com.peprally.jeremy.peprally.custom.Comment;
import com.peprally.jeremy.peprally.data.UserPost;
import com.peprally.jeremy.peprally.model.BaseResponse;
import com.peprally.jeremy.peprally.model.PostDeletionResponse;
import com.peprally.jeremy.peprally.model.PostFeedResponse;
import com.peprally.jeremy.peprally.model.PostResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PostService {

    @POST("/post/like")
    Call<BaseResponse> likePost(@Query("notification_type") int notificationType,
                                @Query("sender") String sender,
                                @Query("receiver") String receiver,
                                @Query("sender_fb_id") String senderFbId,
                                @Query("post_id") String postId);

    @POST("/post/new_post")
    Call<PostResponse> makeNewPost(@Query("username") String username,
                                   @Query("post_id") String postId,
                                   @Query("cognito_id") String cognitoId,
                                   @Query("facebook_id") String facebookId,
                                   @Query("first_name") String firstname,
                                   @Query("text_content") String textContent,
                                   @Query("time_stamp_seconds") long timeStampSeconds);

    @GET("/post/feed")
    Call<PostFeedResponse> getPostFeed();

    @GET("/post/user_post")
    Call<PostResponse> getPost(@Query("username") String username,
                               @Query("time_stamp_seconds") long timeStampSeconds);

    @GET("/post/update_post")
    Call<PostDeletionResponse> updatePost(@Query("post") UserPost post);

}
