package com.peprally.jeremy.peprally.model;

import com.peprally.jeremy.peprally.data.UserPost;

public class PostResponse extends BaseResponse {

    UserPost post;

    public UserPost getPost() {
        return post;
    }
}
