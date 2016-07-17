package com.peprally.jeremy.peprally.db_models;

import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "UserNotifications")
public class DBUserNotification implements Comparable<DBUserNotification>{
    private String nickname;
    private String nicknameSender;
    private String facebookIDSender;
    private String comment;
    private String postID;
    private String commentID;
    private String timeStamp;
    private Long timeInSeconds;
    // 0 = direct fistbump, 1 = comment on post, 2 = fistbump on post, 3 = fistbump on comment
    private int type;

    @Override
    public int compareTo(@NonNull DBUserNotification another) {
        return timeInSeconds.compareTo(another.timeInSeconds);
    }

    @DynamoDBHashKey(attributeName = "Nickname")
    @DynamoDBIndexHashKey(globalSecondaryIndexNames = {"Nickname-PostID-index", "Nickname-CommentID-index"}, attributeName = "Nickname")
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @DynamoDBRangeKey(attributeName = "TimeStampInSeconds")
    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    @DynamoDBAttribute(attributeName = "NicknameSender")
    public String getNicknameSender() {
        return nicknameSender;
    }

    public void setNicknameSender(String nicknameSender) {
        this.nicknameSender = nicknameSender;
    }

    @DynamoDBAttribute(attributeName = "FacebookIDSender")
    public String getFacebookIDSender() {
        return facebookIDSender;
    }

    public void setFacebookIDSender(String facebookIDSender) {
        this.facebookIDSender = facebookIDSender;
    }

    @DynamoDBAttribute(attributeName = "Comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexNames = {"PostID-index", "PostID-CommentID-index"}, attributeName = "PostID")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "Nickname-PostID-index", attributeName = "PostID")
    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexNames = "CommentID-index", attributeName = "CommentID")
    @DynamoDBIndexRangeKey(globalSecondaryIndexNames = {"Nickname-CommentID-index", "PostID-CommentID-index"}, attributeName = "CommentID")
    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    @DynamoDBAttribute(attributeName = "TimeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @DynamoDBAttribute(attributeName = "Type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
