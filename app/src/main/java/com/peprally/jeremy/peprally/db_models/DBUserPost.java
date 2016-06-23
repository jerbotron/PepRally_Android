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
    private int numberOfLikes;
    private int numberOfComments;
    private Set<String> likedUsers;
    private Set<String> dislikedUsers;

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

    @DynamoDBAttribute(attributeName = "NumberOfLikes")
    public int getNumberOfLikes() {
        return numberOfLikes;
    }

    public void setNumberOfLikes(int numberOfLikes) {
        this.numberOfLikes = numberOfLikes;
    }

    @DynamoDBAttribute(attributeName = "NumberOfComments")
    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    @DynamoDBAttribute(attributeName = "LikedUsers")
    public Set<String> getLikedUsers() {
        return likedUsers;
    }

    public void setLikedUsers(Set<String> likedUsers) {
        this.likedUsers = likedUsers;
    }

    public void addLikedUsers(String user) {
        likedUsers.add(user);
    }

    public void removeLikedUsers(String user) {
        likedUsers.remove(user);
    }

    @DynamoDBAttribute(attributeName = "DislikedUsers")
    public Set<String> getDislikedUsers() {
        return dislikedUsers;
    }

    public void setDislikedUsers(Set<String> dislikedUsers) {
        this.dislikedUsers = dislikedUsers;
    }

    public void adddislikedUsers(String user) {
        dislikedUsers.add(user);
    }

    public void removedislikedUsers(String user) {
        dislikedUsers.remove(user);
    }
}
