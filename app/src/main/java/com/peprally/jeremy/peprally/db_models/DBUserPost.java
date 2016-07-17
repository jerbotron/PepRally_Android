package com.peprally.jeremy.peprally.db_models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@DynamoDBTable(tableName = "UserPosts")
public class DBUserPost implements Comparable<DBUserPost>, Parcelable{
    private String nickname;
    private String postID;
    private String cognitoID;
    private String facebookID;
    private String firstname;
    private String timeStamp;
    private String textContent;
    private Long timeInSeconds;
    private int fistbumpsCount;
    private int commentsCount;
    private Set<String> fistbumpedUsers;

    // Public Constructor

    public DBUserPost() {}

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

    public void incrementFistbumpsCount() {
        fistbumpsCount += 1;
    }

    public void decrementFistbumpsCount() {
        fistbumpsCount -= 1;
    }

    public void setFistbumpsCount(int fistbumpsCount) {
        this.fistbumpsCount = fistbumpsCount;
    }

    @DynamoDBAttribute(attributeName = "CommentsCount")
    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void incrementCommentsCount() {
        commentsCount += 1;
    }

    public void decrementCommentsCount() {
        commentsCount -= 1;
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

    // Parcelable Methods

    // Parcel Constructor
    private DBUserPost(Parcel in) {
        this.nickname = in.readString();
        this.postID = in.readString();
        this.cognitoID = in.readString();
        this.facebookID = in.readString();
        this.firstname = in.readString();
        this.timeStamp = in.readString();
        this.textContent = in.readString();
        this.timeInSeconds = in.readLong();
        this.fistbumpsCount = in.readInt();
        this.commentsCount = in.readInt();
        this.fistbumpedUsers = new HashSet<>(Arrays.asList(in.createStringArray()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nickname);
        dest.writeString(postID);
        dest.writeString(cognitoID);
        dest.writeString(facebookID);
        dest.writeString(firstname);
        dest.writeString(timeStamp);
        dest.writeString(textContent);
        dest.writeLong(timeInSeconds);
        dest.writeInt(fistbumpsCount);
        dest.writeInt(commentsCount);
        dest.writeStringArray(fistbumpedUsers.toArray(new String[fistbumpedUsers.size()]));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<DBUserPost> CREATOR = new Parcelable.Creator<DBUserPost>() {
        @Override
        public DBUserPost createFromParcel(Parcel source) {
            return new DBUserPost(source);
        }

        @Override
        public DBUserPost[] newArray(int size) {
            return new DBUserPost[size];
        }
    };
}
