package com.peprally.jeremy.peprally.db_models;

import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

@DynamoDBTable(tableName = "UserNotifications")
public class DBUserNotification implements Comparable<DBUserNotification> {
    private String username;
    private String senderUsername;
    private String facebookIdSender;
    private String postId;
    private String commentId;
    private String comment;
    private Long timeInSeconds;
    // 0 = direct fistbump, 1 = direct message, 2 = post comment, 3 = post fistbump, 4 = comment fistbump
    private int notificationType;

    @Override
    public int compareTo(@NonNull DBUserNotification another) {
        return timeInSeconds.compareTo(another.timeInSeconds);
    }

    @DynamoDBHashKey(attributeName = "Username")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBRangeKey(attributeName = "TimestampSeconds")
    public long getTimeInSeconds() {
        return timeInSeconds;
    }
    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "SenderUsername-index", attributeName = "SenderUsername")
    @DynamoDBIndexRangeKey(globalSecondaryIndexNames = {"PostId-SenderUsername-index", "CommentId-SenderUsername-index"}, attributeName = "SenderUsername")
    public String getSenderUsername() {
        return senderUsername;
    }
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    @DynamoDBAttribute(attributeName = "FacebookIdSender")
    public String getFacebookIdSender() {
        return facebookIdSender;
    }
    public void setFacebookIdSender(String facebookIdSender) { this.facebookIdSender = facebookIdSender; }

    @DynamoDBIndexHashKey(globalSecondaryIndexNames = {"PostId-index", "PostId-CommentId-index", "PostId-SenderUsername-index"}, attributeName = "PostId")
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CommentId-SenderUsername-index", attributeName = "CommentId")
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "PostId-CommentId-index", attributeName = "CommentId")
    public String getCommentId() {
        return commentId;
    }
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    @DynamoDBAttribute(attributeName = "Comment")
    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    @DynamoDBAttribute(attributeName = "NotificationType")
    public int getNotificationType() { return notificationType; }
    public void setNotificationType(int notificationType) { this.notificationType = notificationType; }
}
