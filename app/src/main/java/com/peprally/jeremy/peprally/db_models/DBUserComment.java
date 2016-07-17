package com.peprally.jeremy.peprally.db_models;

import android.os.Parcel;
import android.os.Parcelable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@DynamoDBTable(tableName = "UserComments")
public class DBUserComment implements Parcelable {
    private String postID;
    private String commentID;
    private String nickname;
    private String postNickname;
    private String cognitoID;
    private String facebookID;
    private String timeStamp;
    private String textContent;
    private long timeInSeconds;
    private int fistbumpsCount;
    private Set<String> fistbumpedUsers;

    // Public Constructor
    public DBUserComment() {}

    @DynamoDBHashKey(attributeName = "PostID")
    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    @DynamoDBRangeKey(attributeName = "TimeInSeconds")
    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    @DynamoDBAttribute(attributeName = "CommentID")
    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    @DynamoDBAttribute(attributeName = "Nickname")
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @DynamoDBAttribute(attributeName = "PostNickname")
    public String getPostNickname() {
        return postNickname;
    }

    public void setPostNickname(String postNickname) {
        this.postNickname = postNickname;
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
    private DBUserComment(Parcel in) {
        this.postID = in.readString();
        this.commentID = in.readString();
        this.nickname = in.readString();
        this.postNickname = in.readString();
        this.cognitoID = in.readString();
        this.facebookID = in.readString();
        this.timeStamp = in.readString();
        this.textContent = in.readString();
        this.timeInSeconds = in.readLong();
        this.fistbumpsCount = in.readInt();
        this.fistbumpedUsers = new HashSet<>(Arrays.asList(in.createStringArray()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postID);
        dest.writeString(commentID);
        dest.writeString(nickname);
        dest.writeString(postNickname);
        dest.writeString(cognitoID);
        dest.writeString(facebookID);
        dest.writeString(timeStamp);
        dest.writeString(textContent);
        dest.writeLong(timeInSeconds);
        dest.writeInt(fistbumpsCount);
        dest.writeStringArray(fistbumpedUsers.toArray(new String[fistbumpedUsers.size()]));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<DBUserComment> CREATOR = new Parcelable.Creator<DBUserComment>() {
        @Override
        public DBUserComment createFromParcel(Parcel source) {
            return new DBUserComment(source);
        }

        @Override
        public DBUserComment[] newArray(int size) {
            return new DBUserComment[size];
        }
    };
}
