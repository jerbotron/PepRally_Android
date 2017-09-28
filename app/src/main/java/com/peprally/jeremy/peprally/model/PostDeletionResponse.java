package com.peprally.jeremy.peprally.model;

public class PostDeletionResponse extends BaseResponse {

    boolean isPostDeleted;
    boolean isCommentDeleted;

    public boolean isPostDeleted() {
        return isPostDeleted;
    }

    public boolean isCommentDeleted() {
        return isCommentDeleted;
    }
}
