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
    private String commentId;
    private String nickname;
    private String postNickname;
    private String firstname;
    private String cognitoId;
    private String facebookId;
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

    @DynamoDBAttribute(attributeName = "CommentId")
    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
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

    @DynamoDBAttribute(attributeName = "Fistname")
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    @DynamoDBAttribute(attributeName = "CognitoId")
    public String getCognitoId() {
        return cognitoId;
    }

    public void setCognitoId(String cognitoId) {
        this.cognitoId = cognitoId;
    }

    @DynamoDBAttribute(attributeName = "FacebookId")
    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
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
        this.commentId = in.readString();
        this.nickname = in.readString();
        this.postNickname = in.readString();
        this.firstname = in.readString();
        this.cognitoId = in.readString();
        this.facebookId = in.readString();
        this.timeStamp = in.readString();
        this.textContent = in.readString();
        this.timeInSeconds = in.readLong();
        this.fistbumpsCount = in.readInt();
        this.fistbumpedUsers = new HashSet<>(Arrays.asList(in.createStringArray()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postID);
        dest.writeString(commentId);
        dest.writeString(nickname);
        dest.writeString(postNickname);
        dest.writeString(firstname);
        dest.writeString(cognitoId);
        dest.writeString(facebookId);
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
