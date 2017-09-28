package com.peprally.jeremy.peprally.model;

import com.peprally.jeremy.peprally.data.UserPost;

import java.util.List;

public class PostFeedResponse extends BaseResponse {
    List<UserPost> posts;

    public List<UserPost> getPosts() {
        return posts;
    }
}
