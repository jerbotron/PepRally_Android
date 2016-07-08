package com.peprally.jeremy.peprally.db_models;

import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.util.Set;

@DynamoDBTable(tableName = "UserPosts")
public class DBUserPost implements Comparable<DBUserPost>{
    private String nickname;
    private Long timeInSeconds;
    private String postID;
    private String cognitoID;
    private String facebookID;
    private String firstname;
    private String timeStamp;
    private String textContent;
    private int fistbumpsCount;
    private int numberOfComments;
    private Set<String> fistbumpedUsers;

    @Override
    public int compareTo(@NonNull DBUserPost another) {
        return timeInSeconds.compareTo(another.timeInSeconds);
    }

    @DynamoDBHashKey(attributeName = "Nickname")
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @DynamoDBRangeKey(attributeName = "TimeInSeconds")
    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    @DynamoDBAttribute(attributeName = "PostID")
    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    @DynamoDBAttribute(attributeName = "CognitoID")
    public String getCognitoID() {
        return cognitoID;
    }

    public void setCognitoID(String cognitoID) {
        this.cognitoID = cognitoID;
    }

    @DynamoDBAttribute(attributeName = "FacebookID")
    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    @DynamoDBAttribute(attributeName = "Firstname")
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @DynamoDBAttribute(attributeName = "TimeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @DynamoDBAttribute(attributeName = "TextContent")
    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    @DynamoDBAttribute(attributeName = "FistbumpsCount")
    public int getFistbumpsCount() {
        return fistbumpsCount;
    }

    public void setFistbumpsCount(int fistbumpsCount) {
        this.fistbumpsCount = fistbumpsCount;
    }

    @DynamoDBAttribute(attributeName = "CommentsCount")
    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    @DynamoDBAttribute(attributeName = "FistbumpedUsers")
    public Set<String> getFistbumpedUsers() {
        return fistbumpedUsers;
    }

    public void setFistbumpedUsers(Set<String> fistbumpedUsers) {
        this.fistbumpedUsers = fistbumpedUsers;
    }

    public void addFistbumpedUser(String user) {
        fistbumpedUsers.add(user);
    }

    public void removeFistbumpedUser(String user) {
        fistbumpedUsers.remove(user);
    }
}
